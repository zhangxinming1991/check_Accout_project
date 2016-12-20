// console.debug('config services/providers');

// 检测后端告知请求是否成功
function isOkResBody(resBody) {
    return resBody.flag === 0 || (resBody.flag === undefined && resBody.data !== undefined);
}

Object.defineProperty(
    Object.prototype,
    'renameProperty',
    {
        writable: false, // Cannot alter this property
        enumerable: false, // Will not show up in a for-in loop.
        configurable: false, // Cannot be deleted via the delete operator
        value: function (oldName, newName, fnc) {
            // Do nothing if the names are the same
            if (oldName == newName) {
                return this;
            }
            // Check for the old property name to
            // avoid a ReferenceError in strict mode.
            if (this.hasOwnProperty(oldName)) {
                this[newName] = fnc !== undefined ? fnc(this[oldName]) : this[oldName];
                delete this[oldName];
            }
            return this;
        }
    }
);

function requireReauth() {
    loginModal.modal('show');
}

// 后端请求相关服务
app.factory('HttpReqService', ['$http', '$q', function (http, Q) {
    var svc = {};
    /**
     *
     * @param url
     * @param reqbody
     * @param okfnc 在HTTP响应成功时，用以解析返回数据为promise中resolve数据的函数，其参数是HTTP响应的主体数据。
     * @returns {promise|*|d.promise|d}
     */
    svc.req = function (url, reqbody, okfnc, errfnc) {
        console.debug('url, req:', url, reqbody);
        var deferred = Q.defer();
        try {
            var encoded = reqbody === undefined || angular.equals(reqbody, {}) ? undefined : Encrypt(/*JSON.stringify*/(reqbody));
            //console.debug('encoded:' + encoded);
            http.post(url, /*reqbody*/ encoded).then(function (resPkg) {
                // console.debug('recvd:',resPkg.data);
                resPkg.data = Decrypt(resPkg.data);
                // console.debug('Decrypted:', resPkg.data);
                resPkg.data = resPkg === undefined ? undefined : JSON.parse(resPkg.data);
                var resbody = resPkg.data;
                console.debug('req recvd(parsed as json) Decrypted:', angular.copy(resPkg.data));
                if (isOkResBody(resbody)) {
                    (function preExtractData(resbody) {
                        if ($.isArray(resbody.data) && !resbody.items) {
                            resbody.items = resbody.data;
                            return resbody;
                        }
                    })(resbody);

                    var ret;
                    if (okfnc) {
                        ret = okfnc(resbody);
                        if (!ret) {
                            console.debug('okfnc defined but no return value!!!');
                        }
                    } else {
                        /*okfnc = okfnc || function (resbody) {
                         return resbody.data;
                         };*/
                        ret = resbody.data;
                    }
                    // extract data by provided function, or return resbody.data if function not provided or returns nothing
                    deferred.resolve(ret);
                } else {
                    console.debug('negative resbody:', resbody);
                    errfnc = errfnc || function (resbody) {
                            return {msg: resbody.errmsg || '网络请求错误'};
                        };
                    deferred.reject(errfnc(resbody));
                }
            }, function (errPkg) {
                console.error('请求包 响应包', reqbody, errPkg);
                deferred.reject({msg: '网络或系统错误'});
            }).finally(function () {
            });
        } catch (expt) {
            console.error(expt);
            deferred.reject({msg: '未知错误'});
        }
        console.debug('req result:', deferred.promise);
        return deferred.promise;
    };
    return svc;
}]);

// 用户账户相关服务
app.factory('AccountService', ['$rootScope', '$cookies', '$q', 'HttpReqService', 'FncRmindService', function (rootsgop, cuki, Q, Req, NSvc) {
    // console.debug('constructing AccountService');

    var svc = {};

    // svc.signIn = function signIn(uid, upwd, role) {
    //     return signin({uid: uid, upwd: upwd, role: role});
    // }

    /**
     *根据用户名和用户密码登陆
     * @param formUser 两个字段uid:用户名，upwd：用户密码
     * @returns {d.promise|*|promise|d} promise在resolved状态下是用户信息对象，rejected状态下是一个错误信息对象
     */
    var userStorageKey = 'formUser';


    svc.isAuthenticated = function () {
        return rootsgop.loggedInUser;
    };


    (function readAuthEnv() {
        var storedUser = getStoredUser();
        if (storedUser) {
            rootsgop.loggedInUser = storedUser;
        }
        // console.debug('read auth, have auth info=', svc.isAuthenticated());
    })();

    function getStoredUser(storage) {
        storage = storage || sessionStorage;
        try {
            return JSON.parse(storage.getItem(userStorageKey));
        } catch (expt) {
            console.error(expt);
            return undefined;
        }
    }

    function storeUser(user, storage) {
        storage = storage || sessionStorage;
        try {
            if (user) {
                storage.setItem(userStorageKey, JSON.stringify(user));
            } else {
                storage.removeItem(userStorageKey);
            }
        } catch (expt) {
            console.error(expt);
        }
    }

// 登陆
    svc.signIn = function signIn(formUser) {

        var deferred = Q.defer();
        // store formUser for reauth
        try {
            var reqUser = {
                "uid": formUser.uid,
                "upwd": formUser.upwd,
                // 浏览器登陆标识
                "from": frontBackEndMappping[formUser.role]
            };
            if (!formUser.rememberMe) {
                storeUser(undefined, localStorage);
            } else {
                storeUser(reqUser, localStorage);
            }
            Req.req(ReqUrl.signIn, reqUser, function (resbody) {
                return resbody;
            }).then(function (resbody) {
                // successful login
                var userInfo = resbody.user;
                // store info locally(cookieStore), token
                rootsgop.loggedInUser = userInfo;
                // var token = resbody.token;
                // formUser.token = token;
                // // add auth headers
                // $http.defaults.headers.common['X-Access-Token'] = token;
                if (formUser.rememberMe) {
                    storeUser(reqUser, localStorage);
                }
                storeUser($.extend({}, reqUser, userInfo));

                // var newItemsNum = resbody.isnewpay ? resbody.newpay_num : 0;
                // console.debug('Login newItemsNum:' + newItemsNum);
                // NSvc.hasNotReadNumFromLogin = true;
                // NSvc.newItemsNum = newItemsNum;

                deferred.resolve(userInfo);
            }, function (errMsgObj) {
                console.warn(errMsgObj);
                deferred.reject(errMsgObj);
            });
        } catch (expt) {
            console.error(expt);
            deferred.reject({msg: '网络或系统错误'});
        }

        return deferred.promise;
    };

// 浏览器标签页内自动登陆
    svc.signInBySessionStorage = function () {
        // console.debug('sign in by session storeage');
        var deferred = Q.defer();
        try {
            var stored = getStoredUser();
            if (stored && stored.uid && stored.upwd) {
                return svc.signIn(stored);
            }
        } catch (expt) {
            console.error(expt);
            deferred.reject({msg: '网络或系统错误'});
        }
        deferred.reject({});
        return deferred.promise;
    };

    svc.signInByLocalStorage = function () {
        var deferred = Q.defer();
        try {
            // remember me checked? && uid&upwd existed?
            var storage = getStoredUser(localStorage);
            if (storage && storage.uid && storage.upwd) {
                return svc.signIn(storage);
            } else {
                deferred.reject({msg: '未设置自动登陆'});
            }
        } catch (expt) {
            console.error(expt);
            deferred.reject({msg: '网络或系统错误'});
        }
        return deferred.promise;
    };

    svc.removeStoredSignLocalInfo = function () {
        storeUser(undefined);
        storeUser(undefined, localStorage);
    };

// 注销（登出）
    svc.signOut = function () {
        if (rootsgop.loggedInUser) {
            // console.debug('remote request to sign out');
            // remove logged in user object
            delete rootsgop.loggedInUser;
            // process cookie
            storeUser(undefined);

            Req.req(ReqUrl.signOut); // ignore response
            // $http.defaults.headers.common['X-Access-Token'] = '';
        }
    };

// 注册
    svc.signUp = function (formUser) {
        return Req.req(ReqUrl.signUp, formUser, function (resbody) {
            return resbody.user;
        });
    };

    //找回密码相关
    svc.rstPwdSendVC = function (form) {
        return Req.req(ReqUrl.rstPwdSendvc, form);
    };
    svc.resetPwd = function (form) {
        return Req.req(ReqUrl.rstPwd, form);
    };

// 代理商列表获取，为注册时提供自动补全输入以及避免输入不存在的代理商
    svc.sellerAgents = function () {
        var deferred = Q.defer();
        if (svc.agents && svc.agents.length)
            deferred.resolve(svc.agents);
        Req.req(ReqUrl.fetchAgents).then(function (items) {
            svc.agents = items;
            deferred.resolve(items);
        }, function (errobj) {
            deferred.reject(errobj);
        });
        return deferred.promise;
    };

// 注册申请中的对账联系人列表
    svc.regPendingPaymentNotifiers = function (reqParams) {
        return Req.req(ReqUrl.regPendingNotifiers, $.extend({watch_type: 'reg_cp'}, reqParams), function (resbody) {
            var items = resbody.data;
            items.registerWays = appConf.userRegisterWays;
            return resbody;
        });
    };

// 注册申请中的财务员列表
    svc.regPendingFinancialWorker = function (reqParams) {
        return Req.req(ReqUrl.regPendingFworkers, $.extend({watch_type: 'reg_as'}, reqParams), function (resbody) {
            return resbody;
        });
    };

    //注册申请中的代理商方面的管理员
    svc.regPendingFinancialAdmins = function (reqParams) {
        return Req.req(ReqUrl.regPendingFAdmins, $.extend({watch_type: 'reg_am'}, reqParams), function (resbody) {
            return resbody;
        });
    };

// 拒绝对账联系人的注册申请
    svc.acceptNotifierReg = function (uid) {
        return Req.req(ReqUrl.approveNotifier, {
            id: uid
            , reg_type: 'cp'
            , regflag: 0
        });
    };

// 接受
    svc.rejectNotifierReg = function (uid) {
        return Req.req(ReqUrl.approveNotifier, {
            id: uid
            , reg_type: 'cp'
            , regflag: -2
        });
    };

// 接受财务员的注册申请
    svc.acceptFinancialWorkerReg = function (uid) {
        return Req.req(ReqUrl.approveNotifier, {
            id: uid
            , reg_type: 'as'
            , regflag: 0
        });
    };

// 拒绝财务员的注册申请
    svc.rejectFinancialWorkerReg = function (uid) {
        return Req.req(ReqUrl.approveNotifier, {
            id: uid
            , reg_type: 'as'
            , regflag: -2
        });
    };

// 接受财务员的注册申请
    svc.acceptFinancialAdminReg = function (uid) {
        return Req.req(ReqUrl.approveFAdmin, {
            id: uid
            , reg_type: 'am'
            , regflag: 0
        });
    };

// 拒绝财务员的注册申请
    svc.rejectFinancialAdminReg = function (uid) {
        return Req.req(ReqUrl.approveFAdmin, {
            id: uid
            , reg_type: 'am'
            , regflag: -2
        });
    };

// 对账联系人列表
    svc.paymentNotifiers = function (reqParams) {
        return Req.req(ReqUrl.notifiers, $.extend({
            watch_type: 'reged_cp'
        }, reqParams), function (resbody) {
            var items = resbody.data;
            items.forEach(function (ele) {
                ele.ctlflag = ele.ctlflag || ele.flag;
            });
            items.registerWays = appConf.userRegisterWays;
            return resbody;
        });
    };

// 财务员列表
    svc.financialWorkers = function (reqParams) {
        return Req.req(ReqUrl.fworkers, $.extend({
            watch_type: 'reged_as'
        }, reqParams), function (resbody) {
            resbody.data.forEach(function (ele) {
                ele.ctlflag = ele.flag;
            });
            return resbody;
        });
    };

    // 代理商方面管理员列表
    svc.financialAdmins = function (reqParams) {
        return Req.req(ReqUrl.fadmins, $.extend({
            watch_type: 'reged_am'
        }, reqParams), function (resbody) {
            var items = resbody.data;
            items.forEach(function (ele) {
                ele.ctlflag = ele.flag;
            });
            return resbody;
        });
    }

// 锁定对账联系人
    svc.lockNotifier = function (id) {
        return Req.req(ReqUrl.ctrlNotifier, {
            id: id
            , control_type: 'cp'
            , ctlflag: -3
        }, function () {
            return -3;
        });
    };

    // 解锁对账联系人
    svc.unlockNotifier = function (id) {
        return Req.req(ReqUrl.ctrlNotifier, {
            id: id
            , control_type: 'cp'
            , ctlflag: 0
        }, function () {
            return 0;
        });
    };

// 锁定财务员
    svc.lockFworker = function (id) {
        return Req.req(ReqUrl.ctrlNotifier, {
            id: id
            , control_type: 'as'
            , ctlflag: -3
        }, function () {
            return -3;
        });
    };

    // 解锁财务员
    svc.unlockFworker = function (id) {
        return Req.req(ReqUrl.ctrlNotifier, {
            id: id
            , control_type: 'as'
            , ctlflag: 0
        }, function () {
            return 0;
        });
    };

    // 锁定代理商管理员
    svc.lockFAdmin = function (id) {
        return Req.req(ReqUrl.ctrlFAdmin, {
            id: id
            , control_type: 'am'
            , ctlflag: -3
        }, function () {
            return -3;
        });
    };
    // 解锁代理商管理员
    svc.unlockFAdmin = function (id) {
        return Req.req(ReqUrl.ctrlFAdmin, {
            id: id
            , control_type: 'am'
            , ctlflag: 0
        }, function () {
            return 0;
        });
    };

    return svc;
}]);

/*
 app.factory('OrderService', ['$http', '$q', function ($http, $q) {
 console.debug('construct OrderService');

 var svc = {};

 svc.orders = function () {
 var deferred = $q.defer();
 try {
 var reqBody = {
 "table_name": "ori_account",//查看资源名称
 "watch_type": 'T'//方式
 };
 $http.post(ReqUrl.fwOrders, reqBody).then(function (resPkg) {
 var resBody = resPkg.data;
 if (!resBody.flag) {
 deferred.resolve(resBody.data);
 } else {
 deferred.reject({msg: resBody.errmsg || '无详更细信息'});
 }
 }, function (err) {
 deferred.reject({msg: '网络或系统错误'});
 });
 return deferred.promise;
 }
 catch (expt) {
 console.error(expt);
 deferred.reject({msg: '网络或系统错误'});
 }
 return deferred.promise;
 };

 return svc;
 }]);*/

app.factory('FncRmindService', ['$q', 'HttpReqService', function (Q, Req) {
    var svc = {};

/*    function notifTransFnc(resbody) {
        var resdata = resbody.data;
        var payWaysInItems = [];
        for (var k in resdata) {
            var item = resdata[k];
            if (!item) continue;    // 可能有null……
            item.checkResult = item.checkResult || 'V'; // 设置V标志，为“无”，表示没有设置状态
            item.result = item.checkResult;
            item.payTime = item.payTime || item.uploadTime;
            // item.payTime = new Date(item.payTime);      // 字符串时间转Date类型
            item.mcontract_data = item.manyPay;
            if (payWaysInItems.indexOf(item.payWay) < 0) {
                payWaysInItems.push(item.payWay);
            }
        }
        resdata.payWaysInItems = payWaysInItems;

        return $.extend({}, resbody, {items: resdata});
    }*/

    function notifTransFnc2(resbody) {
        for (var idx = 0; idx < resbody.data.length; idx++) {
            resbody.data[idx].renameProperty("id", "idObj");
            $.extend(resbody.data[idx], resbody.data[idx].idObj);
        }
        return resbody;
    }

    svc.fncReminds = function (cfg) {
        var reqBody = {
            "table_name": "payrecord",//查看资源名称
            "watch_type": 'T'//方式
        };
        $.extend(reqBody, cfg);

        return Req.req(ReqUrl.fwFncReminds, reqBody, notifTransFnc2);
    };

    function opPaymentNotification(approveCfg, type, okfnc) {
        approveCfg["op_result"] = type;
        if (okfnc === undefined) {
            okfnc = function (resbody) {
                return resbody["result"];
            };
        }
        return Req.req(ReqUrl.fwPaymentNotifApprov, approveCfg, okfnc);
    }

    // svc.approvePaymentNotification = function (approveCfg) {
    //     return opPaymentNotification(approveCfg, "Yes");
    // };

// “否决”
    svc.rejectPaymentNotification = function (approveCfg) {
        return opPaymentNotification(approveCfg, "No", function () {
            return 'N';
        });
    };

// “待定”操作
    svc.tbdPaymentNotification = function (approveCfg) {
        return opPaymentNotification(approveCfg, "Wait", function () {
            return 'W';
        });
    };

// 匹配付款通知到出纳
    svc.attachToBankTransaction = function (attachCfg) {
        return Req.req(ReqUrl.attachToTrans, {
            "map_op": "cer_map",
            "pay_id": attachCfg.srcId,
            "bank_id": attachCfg.targetId
        });
    };

    svc.getBankTransactionCandidates = function (reqcfg) {
        return Req.req(ReqUrl.attachToTransCandidates, {
            "map_op": "find_map",
            "pay_id": reqcfg.id
        });
    };

// 预览付款通知（用户上传数据）
    svc.viewNotifications = function (reqParams) {
        return Req.req(ReqUrl.notifView, $.extend({
            watch_type: "T",
            table_name: "pay_cache"
        }, reqParams), notifTransFnc2);
    };

    return svc;
}]);


// 对账相关服务
app.factory('ChkRsltSvc', ['HttpReqService', '$rootScope', function (Req, rootsgop) {
    // console.debug('svc->ChkRsltSvc');

    var svc = {};

    // 如果被刷新，需提取之前的caid
    svc.caid = svc.caid || sessionStorage.getItem('caid');
    rootsgop.caid = svc.caid;

//*******//
//     2016-12-11

    if (!rootsgop.lastUpload) {
        // 浏览器刷新？？
        var str = sessionStorage.getItem(lastUploadInfoKey);
        rootsgop.lastUpload = {};
        $.extend(rootsgop.lastUpload, str ? JSON.parse(str) : {});
    }
    function setLastUploadInfo(lastUploadInfo) {
        rootsgop.lastUpload = rootsgop.lastUpload || {};//保证.lastUpload字段非undefined，$.extend不会在rootsgop增加字段
        $.extend(rootsgop.lastUpload, lastUploadInfo);
        sessionStorage.setItem(lastUploadInfoKey, JSON.stringify(rootsgop.lastUpload));
    }

    //*******//

// 对账操作环境准备
    svc.initCheckingEnv = function () {
        return Req.req(ReqUrl.prepareChkEnv, {}, function (resbody) {
            svc.caid = resbody.caid || resbody.data && resbody.data.caid;
            var lastUpload = {
                time: resbody.lastUploadTime || resbody.data && resbody.data.lastUploadTime
                , result: resbody.lastUploadResult || resbody.data && resbody.data.lastUploadResult
            };
            setLastUploadInfo(lastUpload);
            sessionStorage.setItem('caid', svc.caid);
            rootsgop.caid = svc.caid;
            return {caid: svc.caid};
        });
    };

    // 对账操作
    svc.checkWork = function () {
        return Req.req(ReqUrl.checkWork, {caid: svc.caid}, undefined, function (resbody) {
            return {msg: resbody.error_mes || resbody.errmsg || '无更详细错误信息'};
        });
    };

// 重新对账操作
    svc.reCheck = function () {
        var thisDate = new Date();
        var year = thisDate.getFullYear();
        var month = thisDate.getMonth() + 1;
        return Req.req(ReqUrl.reCheck, {caid: svc.caid, year: year, month: month}, function (resbody) {
            // 2016-12-11
            var lastUpload = {
                time: resbody.lastUploadTime,
                result: resbody.lastUploadResult
            };
            setLastUploadInfo(lastUpload);

            return resbody.data;
        });
    };

    // 历史对账操作
    svc.recheck2 = function (year, month) {
        return Req.req(ReqUrl.recheck2, {year: year, month: month}, function (resbody) {
            var caid = resbody.caid;
            svc.caid = caid;
            sessionStorage.setItem('caid', svc.caid);
            rootsgop.caid = svc.caid;

            // 2016-12-11
            var lastUpload = {
                time: resbody.lastUploadTime,
                result: resbody.lastUploadResult
            };
            setLastUploadInfo(lastUpload);

            return caid;
        });
    };

    // 认可本次对账结果
    svc.acceptCheckingResult = function () {
        return Req.req(ReqUrl.accChkRlt, {caid: svc.caid});
    };

    function extractData(resbody) {
        var resdata = resbody["data"];
        for (var i = 0; i < resdata.length; i++) {
            var item = resdata[i]["basicObject"];
            item.connect = resdata[i]["connectObject"];
            item.inputTime = item.inputTime || item.uploadTime;
            // 字符串时间转Date类型
            // item.inputTime = new Date(item.inputTime);
            resdata[i] = item;
        }
        return resdata;
    }

    // 未关联到合同号或客户名下的出纳
    svc.transactionAttachedToNeitherContractNorClient = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "bfailconnect"}, extractData);
    };

    // 关联到合同号的出纳
    svc.transactionAttachedToContract = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "btocontract"}, extractData);
    };

    // 关联到客户名下的出纳
    svc.transactionAttachedToClient = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "btoclient"}, extractData);
    };

    // 有出纳关联的用户上传付款通知
    svc.notificationAttachedToTransaction = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "phasbinput"}, extractData);
    };

    // 无出纳关联的用户上传付款通知
    svc.notificationNotAttachedToTransaction = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "truepnobinput"}, extractData);
    };

    // 无效的、不可用的、被否决的用户上传付款通知
    svc.notificationInvalid = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "falsepnobinput"}, extractData);
    };

    // 有出纳记录的货款记录
    svc.ordersPaid = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "ohasbinput"}, extractData);
    };

    // 无出纳记录的货款记录
    svc.ordersUnpaid = function () {
        return Req.req(ReqUrl.checkResult, {"watch_type": "onobinput"}, extractData);
    };

    // 获取被导出对账结果报表的文件URL
    svc.checkResultExport = function () {
        return Req.req(ReqUrl.checkResultExport, {caid: svc.caid}, function (resbody) {
            return resbody["cares_url"];
        });
    };
// 历史对账结果获取
    svc.historyResults = function (year) {
        return Req.req(ReqUrl.historyResults, {
                watch_type: "T",
                "table_name": "caresult_history",
                "year": year
            },
            function (resbody) {
                var resdata = resbody.data;
                var yearHistory = {
                    items: resdata
                };
                for (var k in resdata) {
                    var item = resdata[k];
                    var yearMonth = item.month;
                    var r = /^(?:(\d{4})\D)?(\d{1,2})$/;
                    var month = yearMonth.match(r)[2];
                    yearHistory[month] = {
                        url: item.url
                    };
                }
                return yearHistory;
            }
        );
    };

    return svc;
}]);

app.factory('MgmtSvc', ['HttpReqService', function (Req) {
    var svc = {};
    svc.fetchLogs = function (reqParams) {
        return Req.req(ReqUrl.viewLog, $.extend({
            watch_type: 'op_log'
        }, reqParams), function (resbody) {
            var userTypes = appConf.opLogUserRoles;
            var resultTypes = appConf.opLogResultTypes;
            /*resbody.data.forEach(function (ele) {
             if (userTypes.indexOf(ele.usertype) == -1) {
             userTypes.push(ele.usertype);
             }
             // 动态生成
             if(resultTypes.indexOf(ele.result)==-1){
             resultTypes.push(ele.result);
             }
             });*/
            resbody.data['userTypes'] = userTypes;
            resbody.data['resultTypes'] = resultTypes;
            return resbody;
        });
    };

    // 备份数据库
    svc.backupDB = function () {
        return Req.req(ReqUrl.backupdb);
    };

    svc.getDBBackups = function () {
        return Req.req(ReqUrl.dbbackups, undefined, function (resbody) {
            var resdata = resbody.data;
            resdata.forEach(function (ele) {
                var tm = ele.filename.substr(0, ele.filename.indexOf('.sql')).split('_');
                ele.timeStr = tm[0] + '年' + tm[1] + '月' + tm[2] + '日 ' + tm[3] + '时' + tm[4] + '分' + tm[5] + '秒';
            });
            return resdata;
        });
    };

    svc.restoreDB = function (id) {
        return Req.req(ReqUrl.restoredb, {id: id});
    };

    return svc;
}]);

// 积分相关服务
app.factory('ScoreService', ['HttpReqService', function (Req) {
    var svc = {};

    function scoreInfoExtractor(resbody) {
        resbody.items = resbody.items || resbody.data;
        return resbody;
    }

    // 超级管理员获取所有客户的积分信息
    svc.scoreInAllAgents = function (reqParams) {
        return Req.req(ReqUrl.scoreInAllAgents, reqParams, scoreInfoExtractor);
    };

    // 财务员获取客户积分信息
    svc.scoreInAgent = function (reqParams) {
        return Req.req(ReqUrl.scoreInAgent, reqParams, scoreInfoExtractor);
    };

    // 客户积分详情
    svc.scoreDetail = function (reqParams) {
        return Req.req(ReqUrl.scoreDetail, reqParams);
    };
    svc.scoreTableUrl = function () {
        return Req.req(ReqUrl.exportScoreTbl, {}, function (resbody) {
            return resbody.url;
        });
    };

    // 超级管理员 积分管理
    svc.scoreMgmtAll = function (reqParams) {
        return Req.req(ReqUrl.scoreMgmtAll, reqParams, scoreInfoExtractor);
    };
    // 超管 确认礼品
    svc.approveScoreExchange = function (id) {
        return Req.req(ReqUrl.approveScoreExchg, {randKey: id}, function (resbody) {
            return resbody.data || '未领取';
        });
    };
    // 超管 导出积分兑换报表
    svc.scoreExchangeTableUrl = function () {
        return Req.req(ReqUrl.exportScoreExchgTbl, {}, function (resbody) {
            return resbody.url;
        });
    };
    // 超管 导出礼品类型
    svc.giftCategoryUrl = function () {
        return Req.req(ReqUrl.exportGiftCat, {}, function (resbody) {
            return resbody.url;
        });
    };
    // 超管 查看物流详情
    svc.scoreExchangeDetail = function (id) {
        return Req.req(ReqUrl.shipDetail, {randkey: id});
    };

    // 财务员 积分管理
    svc.scoreMgmtInAgent = function (reqParams) {
        return Req.req(ReqUrl.scoreMgmtInAgent, reqParams, scoreInfoExtractor);
    };

    return svc;
}]);