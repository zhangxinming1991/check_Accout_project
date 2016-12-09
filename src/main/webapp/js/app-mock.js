(function () {
    var successfulEcho = true;
    console.debug('检测是否有后台服务器……');
    $.ajax({
        url: ReqUrl.rstPwdSendvc
        , type: 'post'
        , timeout: 2000
        , async: false
        // , crossDomain: true
        // , dataType: 'jsonp'      // jsonp not support sync operation
        , cache: false
        , global: false
        , success: function (data, status) {
        }
        , error: function (res, status, err) {
            if (res.status > 0 && res.status == 404) {
                successfulEcho = false;
            }
        }
    });
    // no mock
    if (successfulEcho) {
        console.info('检测到后台服务器，NO mocking');
        return;
    }

    console.info('无后台服务器，mocking backend...');

    document.write('<script src="js/angular-mocks.js"></script>');
// add requirement
    app.requires.push('ngMockE2E');

    var assert = console.assert;

// var delay = 2000;
    var delay = 0;
    app.config(function ($provide) {
        $provide.decorator('$httpBackend', function ($delegate) {
            var proxy = function (method, url, data, callback, headers) {
                console.debug('mock-> request url', url);
                var encrypedReq = method === 'POST' && url != ReqUrl.fwOrderUpload;
                if (encrypedReq) {
                    // console.debug('request data(before decrypted):', data);
                    data = Decrypt(data);
                }
                var jsonReq = data !== undefined && url != ReqUrl.fwOrderUpload;
                if (jsonReq) {
                    data = JSON.parse(data);
                }
                console.debug('mock-> request data:', data);

                var interceptor = function () {
                    var _this = this,
                        _arguments = arguments;
                    // arguments: status, data, ?, ?
                    if (encrypedReq) {
                        console.debug('response data(before encrypted)', arguments[1]);
                        arguments[1] = Encrypt(/*JSON.stringify*/(arguments[1]));
                        // console.debug('response data(encrypted)', arguments[1]);
                    }

                    setTimeout(function () {
                        callback.apply(_this, _arguments);
                    }, delay);
                };
                return $delegate.call(this, method, url, data, interceptor, headers);
            };
            for (var key in $delegate) {
                proxy[key] = $delegate[key];
            }
            return proxy;
        });
    });

    app.run(['$httpBackend', '$timeout', '$q', '$filter', function (bkd, timeout, Q, $filter) {
        function uuid(len, radix) {
            var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
            var uuid = [], i;
            radix = radix || chars.length;

            if (len) {
                // Compact form
                for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
            } else {
                // rfc4122, version 4 form
                var r;

                // rfc4122 requires these characters
                uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
                uuid[14] = '4';

                // Fill in random data. At i==19 set the high bits of clock sequence as
                // per rfc4122, sec. 4.1.5
                for (i = 0; i < 36; i++) {
                    if (!uuid[i]) {
                        r = 0 | Math.random() * 16;
                        uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                    }
                }
            }
            return uuid.join('');
        }

        // pass through any html file requests
        bkd.whenGET(/.(html)$/).passThrough();

        function failOrNot(failProb) {
            if (!(failProb >= 0 && failProb <= 1)) { // 包括failProb为undefined情况
                failProb = 0.1;
            }
            // 以0.1的概率失败
            return Math.random() < failProb ? -1 : 0;
        }

        var session = {};

        /**
         * 返回a到b的随机数，包含二者
         * @param a
         * @param b
         * @returns {*}
         */
        function randInRange(a, b) {
            if (angular.isDate(a) && angular.isDate(b)) return (function (a, b) {
                return new Date(a.getFullYear() + randInRange(0, b.getFullYear() - a.getFullYear())
                    , a.getMonth() + randInRange(0, b.getMonth() - a.getMonth())
                    , a.getDate() + randInRange(0, b.getDate() - a.getDate()))
            })(a, b);
            else if (angular.isNumber(a)) return (function (a, b) {
                if (arguments.length == 1) {
                    b = a;
                    a = 0;
                }
                return Math.round(Math.random() * (b - a)) + a;
            })(a, b);
        }

        function randIn(items) {
            return items[randInRange(0, ( items.length - 1))];
        }

        // sign in
        bkd.whenPOST(ReqUrl.signIn).respond(function (method, url, reqBody) {
            // data is a string of request
            // reqBody = JSON.parse(reqBody);
            console.debug('mock backend->login: ', reqBody);

            var resUser = {
                "uid": reqBody.uid,
                // "role": reqBody.from,
                "name": '李' + reqBody.uid
                // more info
                , phone: '13312348'
                , email: 'aabcdwfe@a.com'
                , agentname: '广东代理商'
            };
            if (resUser.uid.startsWith('ba')) {
                resUser.role = 'ba';
            } else if (resUser.uid.startsWith('bm')) {
                resUser.role = 'bm';
            } else {
                resUser.role = 'bu';
            }
            var uid = reqBody.uid;
            // if (uid.indexOf('u') != -1) {
            //     resUser.role = 'bu';
            //     resUser.name='财务员'
            // } else
            // if (uid.indexOf('m') != -1) {
            //     resUser.role = 'bm';
            //     resUser.name = '总监';
            // }
            // else if (uid.indexOf('s') != -1) {
            //     resUser.role = 'bs';
            //     resUser.name = '管理员';
            // }
            // else {
            //     resUser.role = 'bu';
            //     resUser.name = '财务员'
            // }

            if (uid == '403')
                return [403, {}, {}];
            if (uid == 'fail')
                return [200, {flag: -1, errmsg: '用户名或密码错误'}];

            var resdata = {
                flag: failOrNot(),
                token: 'token-abc',
                user: resUser,
                // isnewpay: randInRange(0, 9) ? 1 : 0,
                // newpay_num: randInRange(1, 15)
            };

            return [200, resdata, {}];
        });

        // sign up
        bkd.whenPOST(ReqUrl.signUp).respond(function (method, url, reqBody) {
            // reqBody = JSON.parse((reqBody));
            console.debug('mock backend-> register: ', reqBody);

            if (reqBody.username.indexOf('exist') > -1) {
                return [200, {flag: -1, errmsg: '用户名已存在'}];
            }

            return [200, {flag: failOrNot(), user: {uid: reqBody.uid}}];
        });

        // sign out
        bkd.whenPOST(ReqUrl.signOut).respond(function () {
            return [200, {}, {}];
        });

        // 发送验证码
        bkd.whenPOST(ReqUrl.rstPwdSendvc, function (reqbody) {
            return reqboy.username && reqboy.verify_way;
        }).respond(function () {
            return [200, {flag: failOrNot()}];
        });
        // 重置密码请求
        bkd.whenPOST(ReqUrl.rstPwd, function (reqboy) {
            return reqboy.username && reqboy.verify_code;
        }).respond(function () {
            return [200, {flag: failOrNot()}];
        });


        // // get months
        // bkd.whenGET(ReqUrl.fwOrderMonths).respond(function () {
        //     console.debug('mock backend-> get months');
        //     return [200, {flag: 0, data: ['2016-08', '2016-09', '2016-10']}, {}];
        // });

        function orders(itemsLimit) {

            var resdata = [];
            var t =
            {
                "orderNum": "合同号好长好长好长……",
                "input": 1000,
                "debt": 8000,
                "total": 10000,
                "client": "中国铁路",
                "state": "对账中",
                "updateTime": "2016-8-20",
                "remark": "无"
            };
            if (!itemsLimit)itemsLimit = 20;
            for (var i = 0; i < itemsLimit; i++) {
                var tc = angular.copy(t);
                tc.orderNum += '' + i;
                resdata.push(tc);
            }
            return resdata;
        }

        function notifications(itemCount) {
            var resdata = [];
            var t =
            {
                orderNum: '用户上传-',
                // payMoney: 10000,
                receiver: '三一公司',
                payAccount: '621xxxxxx',
                linkCer: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg',
                // linkCer: 'welcome.jpg',
                // creditPicture: 'http://imgsrc.baidu.com/baike/pic/item/96dda144ad345982df652f2f0ef431adcaef84a4.jpg',
                checkResult: '',
                comment: '图片不是很清晰',
                connPerson: '对账联系人X'
            };
            itemCount = itemCount === undefined ? 20 : itemCount;
            for (var i = 0; i < itemCount; i++) {
                var tc = angular.copy(t);
                tc.id = uuid(4, 16);
                tc.payer = '终端用户' + i;
                tc.actualPayer = '实际付款人-' + i;
                tc.orderNum += i;
                tc.payTime = $filter('date')(new Date(randInRange(2014, 2016), randInRange(1, 12), randInRange(1, 28), randInRange(0, 23), randInRange(0, 59), randInRange(0, 59)), appConf.tmFmtLong);
                tc.manyPay = [];
                tc.payWay = randPayWay();
                var payAmount = 0;
                for (var j = 0; j < randInRange(1, 3); j++) {
                    tc.manyPay[j] = {
                        // contract: '合同号...',
                        contract: '货款性质',
                        money: 10000
                    };
                    payAmount += tc.manyPay[j].money;
                }
                tc.payMoney = payAmount;
                resdata.push(tc);
            }
            return resdata;
        }

        function transcash(itemCount) {
            var resdata = [];
            var t = {
                id: '出纳',
                orderNo: '未关联出纳-',
                payee: '收款账户～～',
                payer: '中国铁路15局',
                money: 90000,
                payWay: randPayWay(),
                acountNum: '621xxxxx',
                inputTime: '2016-09',
                status: '状态X',
                comment: '此处省略一万字'
            };
            itemCount = itemCount || 20;
            for (var i = 0; i < itemCount; i++) {
                var tc = angular.copy(t);
                tc.id += i;
                tc.orderNo += i;
                resdata.push(tc);
            }
            return resdata;
        }

        // 订单、出纳、付款通知
        // bkd.whenPOST(ReqUrl.fwOrders, function (reqbody) {
        //     reqbody = JSON.parse(reqbody);
        //     return reqbody.table_name == 'ori_account';
        // }).respond(function (method, url, reqBody) {
        //     reqBody = JSON.parse(reqBody);
        //     console.debug('mock backend->订单', reqBody);
        //     return [200, {flag: failOrNot(), data: orders()}, {}];
        // });

        var numberPerPage = 10;

        var notifsInChk;

        function resPage(pagenum, data, resbody, reqbody) {
            /* if (reqbody.sort) {
             console.debug('server side search', reqbody.search);
             var data = $filter('rmdsFilter')(data, reqbody.search);
             }
             if (reqbody.sort) {
             console.debug('server side sort', reqbody.sort);
             data = $filter('orderBy')(data, reqbody.sort.field, reqbody.sort.desc);
             }*/
            return $.extend(resbody, {
                data: data.slice((pagenum - 1) * numberPerPage, pagenum * numberPerPage)
                // , totalItemCount: data.length
                , totalpage: Math.ceil(data.length / numberPerPage)
            });
        }

        bkd.whenPOST(ReqUrl.fwFncReminds, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.table_name == 'payrecord' && reqbody["watch_type"] == 'T';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend-> 待审付款通知', reqbody);

            notifsInChk = notifsInChk || notifications(randInRange(100, 200));
            var resbody = {
                flag: failOrNot()
            };
            resPage(reqbody.pagenum || 1, notifsInChk, resbody, reqbody);
            return [200, resbody];
        });
        //
        // bkd.whenPOST(ReqUrl.fwFncBankTrans, function (reqbody) {
        //     reqbody = JSON.parse(reqbody);
        //     return reqbody.table_name == 'bankTs';
        // }).respond(function () {
        //     console.debug('mock backend-> 未关联出纳', reqBody);
        //     return [200, {flag: failOrNot, data: transcash()}, {}];
        // });

        // // 付款通知
        // bkd.whenPOST(ReqUrl.fwFncReminds).respond(function () {
        //     console.debug('mock backend-> 待审付款通知');
        //     var resbody = {
        //         flag: failOrNot(),
        //         data: []
        //     };
        //     var t =
        //     {
        //         orderNum: '用户上传->待办',
        //         payer: '中国铁路',
        //         payTime: '2016-08-09',
        //         payMoney: 10000,
        //         payWay: '银行转账',
        //         receiver: '三一公司',
        //         payAccount: '621xxxxxx',
        //         linkCer: 'https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1255741462,4261538489&fm=111&gp=0.jpg',
        //         // creditPicture: 'http://imgsrc.baidu.com/baike/pic/item/96dda144ad345982df652f2f0ef431adcaef84a4.jpg',
        //         result: '???',
        //         comment: '图片不是很清晰'
        //     };
        //     for (var i = 0; i < 30; i++) {
        //         var tc = angular.copy(t);
        //         tc.orderNum += i;
        //         resbody.data.push(tc);
        //     }
        //     return [200, resbody, {}];
        // });

        // 付款通知审核通过操作
        bkd.whenPOST(ReqUrl.fwPaymentNotifApprov).respond(function (method, url, reqBody) {
            console.debug('mock backend->待审付款通知“通过”');
            // reqBody = JSON.parse(reqBody);
            var resbody = {flag: failOrNot()};
            // switch (reqBody.op_result) {
            //     case "Yes":
            //         resbody.result = 'Y';
            //         break;
            //     case 'No':
            //         resbody.result = 'N';
            //         break;
            //     case 'Wait':
            //         resbody.result = 'W';
            //         break;
            //     default:
            //         console.error("INVALID operation on payment notification");
            // }
            return [200, resbody];
        });


        // // “有误”
        // bkd.whenGET(ReqUrl.fwFncBadReminds).respond(function () {
        //     console.debug('mock backend-> bad reminds')
        //
        //     var resbody = {
        //         flag: 0,
        //         data: []
        //     };
        //     var t =
        //     {
        //         payer: '中铁-用户上传->有误',
        //         payTime: '2016-08-29',
        //         payAmount: 10000,
        //         payWay: '银行转账',
        //         payee: '三一公司',
        //         payeeAccount: '621xxxxxxx',
        //         creditPicture: '/img/credit/a.png',
        //         assocResult: '失败',
        //         reason: '凭证图片模糊不清'
        //     };
        //     for (var i = 0; i < 10; i++) {
        //         var tc = angular.copy(t)
        //         tc.payer += i;
        //         resbody.data.push(tc)
        //     }
        //     return [200, resbody, {}];
        // });

        // 上传订单或出纳
        bkd.whenPOST(ReqUrl.fwOrderUpload).respond(function (method, url, reqBody) {
            console.debug('mock backend-> upload file: ', reqBody);
            function sleep(milliseconds) {
                var e = new Date().getTime() + (milliseconds);
                while (new Date().getTime() <= e) {
                }
            }

            var resbody = {
                flag: failOrNot(),
                data: []
            };
            var t =
            {
                "orderNum": "上传后订单-",
                "input": 1000,
                "debt": 8000,
                "total": 10000,
                "client": "中国铁路",
                "state": "对账中",
                "updateTime": "2016-8-20",
                "remark": "无"
            };
            for (var i = 0; i < 10; i++) {
                var tc = angular.copy(t);
                tc.orderNum += '' + i;
                resbody.data.push(tc);
            }
            sleep(1000);
            // resbody.lastUploadTime = $filter('date')(new Date(), 'yyyy-MM-dd HH:mm:ss');
            // resbody.lastUploadResult = randIn(['成功', '失败']);
            console.debug('respond client now', resbody);
            resbody = Encrypt(resbody);
            return [200, resbody];
        });

        // //出纳
        // bkd.whenPOST(ReqUrl.fwFncBankTrans).respond(function (method, url, reqBody) {
        //     console.debug('mock backend-> 未关联出纳');
        //     var resbody = {
        //         flag: failOrNot(),
        //         data: []
        //     };
        //     var t = {
        //         id: '出纳',
        //         orderNo: '未关联出纳-',
        //         payer: '中国铁路15局',
        //         money: 90000,
        //         payWay: '银行转账',
        //         acountNum: '621xxxxx',
        //         inputTime: '2016-09',
        //         status: '状态X',
        //         comment: '此处省略一万字'
        //     };
        //     for (var i = 0; i < 1; i++) {
        //         var tc = angular.copy(t);
        //         tc.id += i;
        //         tc.orderNo += i;
        //         resbody.data.push(tc);
        //     }
        //     return [200, resbody, {}];
        // });

        bkd.whenPOST(ReqUrl.fwAssocTrans).respond(function (method, url, reqBody) {
            console.debug('mock backend -> associate 关联出纳到订单或客户');
            // reqBody = JSON.parse(reqBody);
            var resbody = {};
            if (reqBody.srcId.indexOf('fail') >= 0) {
                resbody.flag = -1;
            } else {
                resbody.flag = failOrNot();
            }

            return [200, resbody, {}];
        });

        bkd.whenPOST(ReqUrl.attachToTransCandidates, function (reqbody) {
            return reqbody.map_op == "find_map" && reqbody.pay_id;
        }).respond(function (method, url, reqbody) {
            // reqbody = JSON.parse(reqbody);
            console.debug("mock backend->待关联付款通知的出纳候选集", reqbody);
            return [200, {flag: failOrNot(), data: transcash(5)}, {}];
        });

        bkd.whenPOST(ReqUrl.attachToTrans, function (reqbody) {
            return reqbody.map_op == "cer_map";
        }).respond(function (method, url, reqBody) {
            // reqBody = JSON.parse(reqBody);
            console.debug('mock backend -> 关联付款通知到出纳', reqBody);
            return [200, {flag: failOrNot()}, {}];
        });

        bkd.whenPOST(ReqUrl.checkWork, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.caid;
        }).respond(function (method, url, reqbody) {
            console.debug('mock -> 对账操作', reqbody);
            return [200, {flag: failOrNot()}, {}];
        });

        function randPayWay() {
            return ['银行转账', '现金', '电汇'][randInRange(0, 2)];
        }

        bkd.whenPOST(ReqUrl.checkResult).respond(function (method, url, reqbody) {
            // reqbody = JSON.parse(reqbody);
            console.debug('mock backend -> 对账结果', reqbody);

            var resdata;
            switch (reqbody["watch_type"]) {
                case "bfailconnect":
                    resdata = transcash(10);
                    for (var i = 0; i < resdata.length; i++) {
                        var item = {
                            basicObject: resdata[i]
                        };
                        resdata[i] = item;
                    }
                    break;
                case "btocontract":
                    resdata = transcash(10);
                    for (var x = 0; x < resdata.length; x++) {
                        var cs = [];
                        var t = {
                            orderNum: '合同号好长好长好长好长好长好长啊～～～',
                            client: '客户',
                            total: 10000,
                            input: 500,
                            debt: 6000,
                        };
                        for (var j = 0; j < 5; j++) {
                            var tc = angular.copy(t);
                            tc.orderNum += j;
                            tc.client += j;
                            cs.push(tc);
                        }
                        var item = {
                            basicObject: resdata[x],
                            connectObject: cs
                        };
                        resdata[x] = item;
                    }
                    break;
                case "btoclient":
                    resdata = transcash(10);
                    for (var y = 0; y < resdata.length; y++) {
                        resdata[y] = {
                            basicObject: resdata[y],
                            connectObject: ['客户' + y]
                        };
                    }
                    break;
                case "phasbinput":
                    resdata = notifications(10);
                    for (var x = 0; x < resdata.length; x++) {
                        var cs = [];
                        var t = {
                            payee: '付款账户-',
                            payer: '付款人X～',
                            money: 10000,
                            inputTime: '2016-09',
                        };
                        for (var j = 0; j < 5; j++) {
                            var tc = angular.copy(t);
                            tc.payee += j;
                            tc.payer += j;
                            tc.payWay = randPayWay();
                            cs.push(tc);
                        }
                        var item = {
                            basicObject: resdata[x],
                            connectObject: cs
                        };
                        resdata[x] = item;
                    }
                    break;
                case "truepnobinput":
                    resdata = notifications(10);
                    for (var i = 0; i < resdata.length; i++) {
                        resdata[i] = {
                            basicObject: resdata[i]
                        }
                    }
                    break;
                case "falsepnobinput":
                    resdata = notifications(20);
                    for (var i = 0; i < resdata.length; i++) {
                        resdata[i] = {
                            basicObject: resdata[i]
                        }
                    }
                    break;
                case "ohasbinput":
                    resdata = orders(10);
                    for (var x = 0; x < resdata.length; x++) {
                        var cs = [];
                        var t = {
                            payee: '付款账户-',
                            payer: '付款人X～',
                            money: 10000,
                            inputTime: '2016-09',
                        };
                        for (var j = 0; j < 5; j++) {
                            var tc = angular.copy(t);
                            tc.payee += j;
                            tc.payer += j;
                            tc.payWay = randPayWay();
                            cs.push(tc);
                        }
                        var item = {
                            basicObject: resdata[x],
                            connectObject: cs
                        };
                        resdata[x] = item;
                    }
                    break;
                case "onobinput":
                    resdata = orders(10);
                    for (var i = 0; i < resdata.length; i++) {
                        resdata[i] = {
                            basicObject: resdata[i]
                        }
                    }
                    break;
                default:
                    console.error('mock 错误，无法响应请求：', reqbody);
            }

            return [200, {flag: failOrNot(), data: resdata}, {}];
        });

        bkd.whenPOST(ReqUrl.checkResultExport).respond(function () {
            console.debug('mock backend-> 对账结果文件URL')
            return [200, {
                flag: failOrNot(),
                cares_url: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg'
            }, {}];
        });

        // 对账历史
        bkd.whenPOST(ReqUrl.historyResults, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == "T" && reqbody.table_name == "caresult_history" && reqbody.year
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend-> 历史对账结果查询', reqbody);
            var resdata = [];
            var s = randInRange(1, 12);
            for (var i = s; i <= randInRange(s, 12); i++) {
                resdata.push({
                    month: "2016-" + i,
                    url: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg'
                });
            }
            return [200, {flag: failOrNot(), data: resdata}, {}];
        });

        bkd.whenPOST(ReqUrl.reCheck, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            assert(reqbody.caid === session.caid, reqbody.caid, session.caid);
            return reqbody.caid;
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend-> 重新对账环境准备', reqbody)
            return [200, {flag: failOrNot(), caid: newCaid()}, {}];
        });

        bkd.whenPOST(ReqUrl.accChkRlt).respond(function (method, url, reqbody) {
            console.debug('mock backend->“返利”');
            return [200, {flag: failOrNot()}];
        });

        function newCaid() {
            var thisDate = new Date();
            var year = thisDate.getFullYear();
            //月份是基于0计算的
            var month = thisDate.getMonth() + 1;
            //然而日期不是，holy shit for the api
            var date = thisDate.getDate();
            if (date < appConf.checkClosingDate) {
                month -= 1;
            }
            if (month <= 0) {
                month = 12;
                year -= 1;
            }
            session.caid = year + '-' + month + '-' + uuid(4);
            return session.caid;
        }

        bkd.whenPOST(ReqUrl.recheck2).respond(function () {
            console.debug('mock backend->重新对账（2）')
            return [200, {flag: failOrNot(), /* caid: newCaid()*/}];
        });

        //
        // bkd.whenPOST(ReqUrl.newNotifNum).respond(function () {
        //     var resdata = {falg: failOrNot(), newpay_num: randInRange(0, 30)};
        //     console.debug('mock backend->新付款通知（用户上传）数目查询，返回' + resdata.newpay_num + '条');
        //     resdata.isnewpay = resdata.newpay_num > 0;
        //     return [200, resdata, {}];
        // });

        var notifsInPreview;
        bkd.whenPOST(ReqUrl.notifView, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'T' && reqbody.table_name == 'pay_cache';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->预览付款通知数据（用户上传）', reqbody);
            notifsInPreview = notifsInPreview || notifications(randInRange(100, 150));
            var resbody = {
                flag: failOrNot()
            };
            resPage(reqbody.pagenum || 1, notifsInPreview, resbody, reqbody);
            return [200, resbody];
        });


        bkd.whenPOST(ReqUrl.prepareChkEnv).respond(function () {
            console.debug('mock->准备对账环境');
            var resdata = {
                flag: failOrNot()
                , data: {
                    caid: newCaid()
                    , lastUploadTime: $filter('date')(new Date(), appConf.tmFmtLong)
                    , lastUploadResult: randIn(['成功', '失败'])
                }
            };
            console.debug('mock backend->准备对账环境， 返回' + resdata.caid);
            return [200, resdata];
        });


        function regNotifiers(itemCount) {
            var itemsNum = itemCount;
            var items = [];
            for (var i = 0; i < itemsNum; i++) {
                var item = {
                    username: '联系人 ' + i
                    , realName: '椿 ' + i
                    , phone: '10086 ' + i
                    , email: 'k' + i + '@sany.com'
                    , weixin: 'wechat-' + i
                    , registerWay: randIn(appConf.userRegisterWays)
                    , company: '恒大地产-' + i
                    , companyid: 'cid-' + i
                    , contractMes: '合同号啊～～～～～'
                    , agent: '代理商 ' + i
                    , flag: randIn([0, -3])
                };
                items.push(item);
            }
            return items;
        }

        function regFworkers(itemCount) {
            var itemsNum = itemCount;
            var items = [];
            for (var i = 0; i < itemsNum; i++) {
                var item = {
                    workId: '财务员 ' + i
                    , name: '湫 ' + i
                    , phone: '10086 ' + i
                    , email: 'q' + i + '@sany.com'
                    // , company: '恒大地产-' + i
                    , companyid: 'cid-' + i
                    , agentid: '代理商 ' + i
                    , flag: randIn([0, -3])
                };
                items.push(item);
            }
            return items;
        }

        function regFAdmins(itemCount) {
            var itemsNum = itemCount;
            var items = [];
            for (var i = 0; i < itemsNum; i++) {
                var item = {
                    workId: '代理商管理 ' + i
                    , name: '湫 ' + i
                    , phone: '10086 ' + i
                    , email: 'q' + i + '@sany.com'
                    // , company: '恒大地产-' + i
                    , companyid: 'cid-' + i
                    , agentid: '代理商 ' + i
                    , flag: randIn([0, -3])
                };
                items.push(item);
            }
            return items;
        }


        var pendingNotifiers;
        bkd.whenPOST(ReqUrl.regPendingNotifiers, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'reg_cp';
        }).respond(function (method, url, reqbody) {
            pendingNotifiers = pendingNotifiers || regNotifiers(randInRange(100, 150));
            return [200, resPage(reqbody.pagenum, pendingNotifiers, {flag: failOrNot()}, reqbody)];
        });

        var pendingWorkers;
        bkd.whenPOST(ReqUrl.regPendingFworkers, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'reg_as';
        }).respond(function (method, url, reqbody) {
            console.debug('mock->客户审阅', reqbody);
            pendingWorkers = pendingWorkers || regFworkers(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, pendingWorkers, {flag: failOrNot()}, reqbody)];
        });

        var pendingFAdmins;
        bkd.whenPOST(ReqUrl.regPendingFAdmins, function (reqbody) {
            return reqbody.watch_type == 'reg_am';
        }).respond(function (method, url, reqbody) {
            console.debug('mock->代理商方面管理员');
            pendingFAdmins = pendingFAdmins || regFAdmins(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, pendingFAdmins, {flag: failOrNot()}, reqbody)];
        });

        bkd.whenPOST(ReqUrl.approveNotifier, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.reg_type == 'cp' && reqbody.regflag == 0
        }).respond(function () {
            console.debug('mock backend->审阅对账联系人注册（通过）');
            return [200, {flag: failOrNot()}, {}];
        });

        bkd.whenPOST(ReqUrl.approveNotifier, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.reg_type == 'cp' && reqbody.regflag == -2
        }).respond(function () {
            console.debug('mock backend->审阅对账联系人注册（拒绝）');
            return [200, {flag: failOrNot()}, {}];
        });

        bkd.whenPOST(ReqUrl.approveFw, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.reg_type == 'as' && reqbody.regflag == 0;
        }).respond(function () {
            console.debug('mock backend->审阅代理商注册（通过）');
            return [200, {flag: failOrNot()}, {}];
        });

        bkd.whenPOST(ReqUrl.approveFw, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.reg_type == 'as' && reqbody.regflag == -2
        }).respond(function () {
            console.debug('mock backend->审阅代理商注册（拒绝）');
            return [200, {flag: failOrNot()}, {}];
        });


        bkd.whenPOST(ReqUrl.approveFAdmin, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.reg_type == 'am' && reqbody.regflag !== undefined;
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->审阅代理商管理员注册', reqbody);
            return [200, {flag: failOrNot()}, {}];
        });

        var regedNotifiers;
        bkd.whenPOST(ReqUrl.notifiers, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'reged_cp';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->已注册对账联系人获取');
            regedNotifiers = regedNotifiers || regNotifiers(randInRange(300, 400));
            return [200, resPage(reqbody.pagenum, regedNotifiers, {flag: failOrNot()}, reqbody)];
        });

        var regedFWorkers;
        bkd.whenPOST(ReqUrl.fworkers, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'reged_as';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->已注册代理商财务员获取');
            regedFWorkers = regFworkers(randInRange(50, 100));
            return [200, resPage(reqbody.pagenum, regedFWorkers, {flag: failOrNot()}, reqbody)];
        });

        var regedFAdmins;
        bkd.whenPOST(ReqUrl.fadmins, function (reqbody) {
            return reqbody.watch_type == 'reged_am';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->已注册代理商管理员获取');
            regedFAdmins = regedFAdmins || regFAdmins(randInRange(50, 100));
            return [200, resPage(reqbody.pagenum, regedFAdmins, {flag: failOrNot()}, reqbody)];
        });

        bkd.whenPOST(ReqUrl.ctrlNotifier, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.control_type == 'cp' && reqbody.ctlflag !== undefined;
        }).respond(function () {
            console.debug('mock backend->控制对账联系人');
            return [200, {flag: failOrNot()}, {}];
        });
        bkd.whenPOST(ReqUrl.ctrlNotifier, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.id && reqbody.control_type == 'as' && reqbody.ctlflag !== undefined;
        }).respond(function () {
            console.debug('mock backend->控制代理商财务员');
            return [200, {flag: failOrNot()}, {}];
        });
        bkd.whenPOST(ReqUrl.ctrlFAdmin, function (reqbody) {
            return reqbody.id && reqbody.control_type == 'am' && reqbody.ctlflag !== undefined;
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend->控制代理商管理员', reqbody);
            return [200, {flag: failOrNot()}, {}];
        });

        bkd.whenPOST(ReqUrl.backupdb).respond(function () {
            console.debug('mock backend->备份数据库');
            return [200, {flag: failOrNot()}];
        });
        bkd.whenPOST(ReqUrl.dbbackups).respond(function () {
            console.debug('mock backend->数据库备份文件列表');
            var resdata = [];
            for (i = 1; i <= 12; i++) {
                var t = {
                    id: uuid()
                    ,
                    filename: '2016_' + randInRange(1, 12) + '_' + randInRange(1, 31) + '_' + randInRange(0, 23) + '_' + randInRange(0, 59) + '_' + randInRange(0, 59) + '.sql'
                };
                resdata.push(t);
            }
            return [200, {flag: failOrNot(), data: resdata}];
        });
        bkd.whenPOST(ReqUrl.restoredb).respond(function (method, url, reqbody) {
            console.debug('mock backend->恢复数据库', reqbody);
            return [200, {flag: failOrNot()}]
        });

        function opLogs(itemCount) {
            var items = [];
            for (var i = 0; i < itemCount; i++) {
                var t = {
                    time: randIn([2015, 2016]) + '-' + randInRange(1, 12) + '-' + randInRange(1, 30),
                    username: randIn(['路飞', '鸣人']),
                    usertype: randIn(appConf.opLogUserRoles),
                    content: randIn(['放炸弹', '种树', '卖烧烤']),
                    result: randIn(['成功', '失败'])
                };
                items.push(t);
            }
            return items;
        }

        var oplogs;
        bkd.whenPOST(ReqUrl.viewLog, function (reqbody) {
            // reqbody = JSON.parse(reqbody);
            return reqbody.watch_type == 'op_log';
        }).respond(function (method, url, reqbody) {
            console.debug('mock backend ->操作日志');
            oplogs = oplogs || opLogs(randInRange(10000, 20000));
            return [200, resPage(reqbody.pagenum, oplogs, {flag: failOrNot()}, reqbody)];
        });

        bkd.whenPOST(ReqUrl.fetchAgents).respond(function (method,url,reqbody) {
            console.debug('mock->代理商列表',reqbody);
            var items = [{code: 'gd0001', name: '广东代理商'}, {code: 'ah0001', name: '安徽代理商'}];
            var extra = 2;
            for (var i = 0; i < randInRange(0, extra); i++) {
                var t = {
                    code: 'xxx-' + i,
                    name: '吼吼' + i
                };
                items.push(t);
            }
            return [200, {flag: failOrNot(0), data: items}];
        });


        function randScoreInfo(itemCount) {
            var items = [];
            for (var i = 0; i < itemCount; i++) {
                var item = {
                    agentName: '代理商'
                    , username: '用户名'
                    , realName: '真实姓名'
                    , weiXin: '微信'
                    , company: '客户名'
                    , score: randInRange(100, 200)
                    , exchangedScore: randInRange(50, 100)
                    , exchangingScore: randInRange(0, 50)
                    , status: randIn(appConf.scoreStatusInTable)
                };
                items.push(item);
            }
            return items;
        }

        var scoreAll;
        bkd.whenPOST(ReqUrl.scoreInAllAgents).respond(function (method, url, reqbody) {
            console.debug('mock->所有客户积分情况',reqbody);
            scoreAll = scoreAll || randScoreInfo(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, scoreAll, {flag: failOrNot()}, reqbody)];
        });
        bkd.whenPOST(ReqUrl.scoreInAgent).respond(function (method, url, reqbody) {
            console.debug('mock->代理商客户积分情况',reqbody);
            scoreAll = scoreAll || randScoreInfo(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, scoreAll, {flag: failOrNot()}, reqbody)];
        });

        function genScoreMgmt(itemCount) {
            var items = [];
            for (var i = 0; i < itemCount; i++) {
                var item = {
                    agentName: '代理商'
                    , username: '用户名'
                    , realName: '真实姓名'
                    , weiXin: '微信'
                    , company: '客户名'
                    , exchangeScore: randInRange(50, 100)
                    , exchangeType: randIn(appConf.exchangeTypes)
                    , exchangeCategory: '礼品类型'
                    , applicaTime: $filter('date')(randInRange(new Date(2014, 1, 1), new Date()), appConf.tmFmtLong)
                    , finishTime: $filter('date')(randInRange(new Date(2014, 1, 1), new Date()), appConf.tmFmtLong)
                    , status: randIn(['兑换中', '已兑换', '未领取'])
                    , description: '说明'
                };
                items.push(item);
            }
            return items;
        }

        var scoreMgmtAll;
        bkd.whenPOST(ReqUrl.scoreMgmtAll).respond(function (method, url, reqbody) {
            console.debug('mock->超级管理员 积分管理表',reqbody);
            scoreMgmtAll = scoreMgmtAll || genScoreMgmt(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, scoreMgmtAll, {flag: failOrNot()}, reqbody)]
        });
        bkd.whenPOST(ReqUrl.approveScoreExchg).respond(function (method, url, reqbody) {
            console.debug('mock->确认礼品兑换',reqbody);
            return [200, {flag: failOrNot()}];
        });
        bkd.whenPOST(ReqUrl.scoreMgmtInAgent).respond(function (method, url, reqbody) {
            console.debug('mock->财务员 积分管理表',reqbody);
            scoreMgmtAll = scoreMgmtAll || genScoreMgmt(randInRange(100, 200));
            return [200, resPage(reqbody.pagenum, scoreMgmtAll, {flag: failOrNot()}, reqbody)]
        });

    }]);
})();