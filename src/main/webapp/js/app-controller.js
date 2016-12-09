// console.debug('config controllers');

var msgOkTimeout = 800;
var msgErrTimeout = 1200;

var loginForm = $('#loginForm');
var loginModal = $('#loginModal');
var loginSubmitBtn = loginForm.find('button[type="submit"]');
var registerForm = $('#signUpForm');
var registerModal = $('#registerModal');
var regSubmitBtn = registerForm.find('button[type="submit"]');
var resetPwdModal = $('#resetPwdModal');
var resetPwdForm = $('#resetPwdForm');
var rstPwdSubmitBtn = resetPwdForm.find('button:submit');
var resetPwdVCRecvBtn = $('#resetPwdVCRecvBtn');
var regLogModal = $('#loginModal,#registerModal');      // reg -> register, log -> login
var signOutEntry = $('div[ng-show="loggedInUser"]');
var regLogEntry = $('div[ng-hide="loggedInUser"]');
var anyModal = $('.modal');

var ng = angular;

// 首页
app.controller('MainCtrl', ['$scope', '$state', '$rootScope', 'AccountService',
    function ($scope, $state, rootsgop, AccSvc) {
        // console.debug('MainCtrl');
        // console.debug('authed?' + AccSvc.isAuthenticated());

        // rebind F5(refresh) to reload current state( refresh data by javascript)
        $scope.disableKeyF5 = function (evt) {
            if ((evt.which || evt.code) === 116 && !evt.ctrlKey) {    // do not disable <Ctrl>+F5 (force refreshing)
                //console.debug('F5 pressed');
                evt.preventDefault();
                // console.debug('reloading state');
                $state.reload();
            }
        };
        $(document).on('keydown', $scope.disableKeyF5);

        anyModal.on('hidden.bs.modal', function () {
            // regLogModal.on('shown.bs.modal', function () {
            $(this).find('div.with-errors').html('');
        });
        // $('.modal').modal('hide');

        // anyModal.on('hidden.bs.modal', function () {
        // });
        // anyModal.on('shown.bs.modal', function () {
        // });

        rootsgop.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
            rootsgop.activeState = toState.name;
        });

        rootsgop.$watch('loggedInUser', function (nv) {
            if (nv) {
                // 先隐藏，再显示
                regLogEntry.css('display', 'none');
                signOutEntry.css('display', 'inherit');
            } else {
                signOutEntry.css('display', 'none');
                regLogEntry.css('display', 'inherit');
            }
        });
    }]);

var homeCtrl = ['$scope', 'AccountService', function (sgop, AccSvc) {
    // console.debug('ctrl-> state-index-ctrl(homeCtrl)');
    // 如果用户在程序内（angular生命周期内的）
    AccSvc.signOut();
    anyModal.modal('hide');
    loginModal.modal('show');
}];

// 登陆
var signInCtrl = ['$scope', '$state', 'AccountService',
    function (sgop, $state, AccountService) {
        //console.debug('sign in ctrl 登陆');

        var ctrl = sgop;
        ctrl.formUser = {};

        sgop.submitForm = function () {
            var formUser = sgop.formUser;

            /*if (loginForm.hasClass('ng-invalid')) {
             ctrl.errMsg = '登陆表单有误';
             return false;
             }*/
            if (!(formUser && formUser.upwd && formUser.uid)) {
                ctrl.errMsg = '登陆名、密码不能为空';
                return false;
            }

            loginSubmitBtn.attr('disabled', true);
            ctrl.errMsg = '登陆中……';
            // // remove stored sign info
            // AccountService.removeStoredSignLocalInfo();
            AccountService.signIn(formUser).then(function (userInfo) {
                    goState(userInfo.role);
                },
                function (err) {
                    ctrl.errMsg = '登陆失败：' + ensureErrMsg(err);
                }).finally(function () {
                loginSubmitBtn.removeAttr('disabled');
            });
        };

        // U<-->财务人员<-->fw;  M<-->财务总监<-->fs; S-->系统管理人员<-->sa
        function goState(role) {
            switch (role) {
                case "bu":
                    if (new Date().getDate() < appConf.checkClosingDate) {
                        $state.go('u.fw.h');
                    } else {
                        $state.go('u.fw');
                    }
                    return;
                case "bm":
                    $state.go('u.fs');
                    return;
                case "bs":
                    $state.go('u.sa');
                    return;
                case 'ba':
                    $state.go('u.fm');
            }
        }

        loginModal.on('hidden.bs.modal', function () {
            ctrl.formUser.upwd = '';
            $(this).find(':password').each(function () {
                this.value = '';
            });
        });
    }];
app.controller('SignInCtrl', signInCtrl);

// 注册
var signUpCtrl = ['$scope', '$state', 'AccountService', '$uibModal', '$timeout',
    function (sgop, $state, AccountService, msgbox, timeout) {
        //console.debug('sign up ctrl-> 注册用户');

        var ctrl = this;
        // for no 'controller as ctrl' grammar
        // sgop.signUpCtrl=sgop;
        // var ctrl = sgop;
        ctrl.formUser = {};

        ctrl.submitForm = function () {

            var pwd = ctrl.formUser.upwd;
            if (!(
                    pwd.length && pwd.length >= 8 && pwd.length <= 16       // 长度限制
                    && /[0-9]/.test(pwd)
                    && /[a-z]/.test(pwd)
                    && /[A-Z]/.test(pwd)
                )
            ) {
                ctrl.errMsg = '密码8-16位，必须有大小写字母和数字三种字符';
                return false;
            }
            var pwdCfm = ctrl.formUser.upwdCfm;
            if (pwdCfm != pwd) {
                ctrl.errMsg = '两次输入的密码不一致';
                return false;
            }

            /* if (registerForm.hasClass('ng-invalid')) {
             ctrl.errMsg = '申请表有误';
             return false;
             }*/
            regSubmitBtn.attr('disabled');
            ctrl.errMsg = '申请注册中……';

            var formUser = angular.copy(ctrl.formUser);
            formUser.role = frontBackEndMappping.userRole[formUser.role] || formUser.role;
            formUser.renameProperty('uid', 'username');
            formUser.renameProperty('upwd', 'password');
            formUser.renameProperty('agent', 'agentid', function (agent) {
                return agent.code;
            });
            AccountService.signUp(formUser).then(function (resData) {
                // $state.go('login');
                // registerModal/*$('.modal')*/.modal('hide');
                anyModal.modal('hide');
                var boxInst = showMsg({
                    msgbox: msgbox,
                    title: '注册结果',
                    msgHtml: '注册<strong class="text-success">成功</strong>，管理员将会审阅您的注册申请。'
                });
                timeout(function () {
                    boxInst.close();
                    loginModal.modal('show');
                }, msgOkTimeout);
            }, function (err) {
                ctrl.errMsg = '注册失败：' + ensureErrMsg(err);
            }).finally(function () {
            });
        };


        registerModal.on('shown.bs.modal', function () {
            if (!sgop.agents || !sgop.agents.length) {
                (function refreshAgents() {
                    AccountService.sellerAgents().then(function (items) {
                        sgop.agents = items;
                    }, function (errObj) {
                    });
                })();
            }
        });

        registerModal.on('hidden.bs.modal', function () {
            ctrl.formUser.upwd = ctrl.formUser.upwdCfm = '';
            $(this).find(':password').each(function () {
                this.value = '';
            });
        });
    }];
app.controller('SignUpCtrl', signUpCtrl);

var resetPwdCtrl = ['$scope', '$state', 'AccountService', '$uibModal', '$interval', '$timeout',
    function (sgop, $state, AccSvc, msgbox, interval, timeout) {
        // console.debug('ctrl->resetPwdCtrl 找回密码');

        var ctrl = this;

        resetPwdModal.on('shown.bs.modal', function () {
            ctrl.form.vc = '';        // failed to reset
        });
        ctrl.vcRecv = function () {
            function vcRecvBtnMsg(msg) {
                ctrl.vcRecvBtnTip.msg = msg;
                ctrl.vcRecvBtnTip.isOpen = true;
                timeout(function () {
                    ctrl.vcRecvBtnTip.isOpen = false;
                }, 1500);
            }

            if (!ctrl.form.uid) {
                vcRecvBtnMsg('请填写用户名');
                return false;
            }

            var wait = 180;
            var remain = wait;
            resetPwdVCRecvBtn.prop('disabled', true);
            var timer = interval(function () {
                resetPwdVCRecvBtn.text('获取验证码（' + remain + '）秒');
                remain--;
                if (remain <= 0) {
                    resetVCRecvBtn();
                }
            }, 1000, wait);

            function resetVCRecvBtn() {
                interval.cancel(timer);
                resetPwdVCRecvBtn.prop('disabled', false);
                resetPwdVCRecvBtn.text('获取验证码');
            }

            AccSvc.rstPwdSendVC({
                username: ctrl.form.uid
                , verify_way: ctrl.form.vcRecvWay
            }).then(function (ok) {
                vcRecvBtnMsg('发送成功，请查收')
            }, function (errObj) {
                vcRecvBtnMsg('发送失败，错误：' + ensureErrMsg(errObj));
                resetVCRecvBtn();
            });
        };

        ctrl.submitForm = function () {
            if (!ctrl.form.uid || !ctrl.form.vc) {
                ctrl.errMsg = '用户名、验证码不能为空';
                return false;
            }
            var msgCfg = {
                msgbox: msgbox
                , title: '重置密码'
                , msgHtml: '操作<strong>进行中……</strong>'
                , important: true
                , noCloseBtn: true
                , ctrl: ['$scope', '$uibModalInstance', function (sgop, boxInst) {
                    sgop.msgCfg = msgCfg;
                    sgop.closeMsgbox = function () {
                        boxInst.close();
                        if (msgCfg.opRlt) {
                            // close messge box
                            loginModal.modal('show');
                        } else {
                            ctrl.errMsg = '操作失败';
                            resetPwdModal.modal('show');
                        }
                    };
                }]
            };
            rstPwdSubmitBtn.prop('disabled', true);
            resetPwdModal.modal('hide');
            var boxInst = showMsg(msgCfg);
            AccSvc.resetPwd({username: ctrl.form.uid, verify_code: ctrl.form.vc})
                .then(function (ok) {
                    msgCfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                    msgCfg.opRlt = true;
                }, function (errObj) {
                    msgCfg.msgHtml = '操作<strong class="text-warning">失败</strong>，错误：' + ensureErrMsg(errObj);
                    msgCfg.opRlt = false;
                })
                .finally(function () {
                    msgCfg.noCloseBtn = false;
                    rstPwdSubmitBtn.prop('disabled', false);

                });
        };
    }];
app.controller('ResetPwdCtrl', resetPwdCtrl);

// 对于直接通过URL访问需要授权信息页面的情况（#/u/*, #/u/fw,#/fs, #/u/sa），Javascript环境已没有任何用户登陆信息，如何自动登陆？
// 已#/u打头的路径，需要授权信息，需检测是否已登陆，如果没有,或者自动登陆、或者弹窗登陆、或者跳转登陆页面
var uCtrl = ['$scope', '$state', 'AccountService', '$timeout', '$uibModal', 'FncRmindService', 'ChkRsltSvc', function (sgop, state, AccSvc, timeout, msgbox, NotifSvc, ChkSvc) {
    // console.debug('uCtrl');

    regLogModal.modal('hide');
    //
    // //如果未登陆
    // if (sgop.loggedInUser === undefined) {
    //     // todo go login?  auto-login? popup-login?
    //     var reLoged = false;
    //     // console.debug('trying to re-login by: ',sessionStorage, sessionStorage.getItem('formUser'));
    //     AccSvc.signInBySessionStorage().then(function () {
    //         //console.debug('自动登陆（session storage）');
    //         reLoged = true;
    //     }, function (fail) {
    //     });
    //     if (!reLoged) {
    //         state.go('index');
    //         return;
    //     }
    // } else {
    //     // logged in ?
    //     // pass through
    // }

    var ctrl = sgop.$root;

    ctrl.signOut = function () {
        AccSvc.signOut();
        state.go('index');
    };


    ctrl.userInfo = function () {
        msgbox.open({
            templateUrl: 'fw-info.html'
            , controller: ['$scope', '$uibModalInstance', function (boxsgop, boxInst) {
                // console.debug('box ctrl');
                boxsgop.closeMsgbox = function () {
                    boxInst.dismiss();
                };
            }]
        });
    };

    /*
     // 页面被销毁时自动注销会导致刷新页面也会注销登陆，这是不允许的
     // 存在问题：利用登陆过本网站的标签页访问其他网站后，继续使用该标签页访问本站时，登陆信息还在（跳转其他网站时没有自动注销登陆）
     // window.addEventListener('beforeunload',function () {
     window.addEventListener('unload', function () {
     // debug
     sessionStorage.setItem('last-close', 'beforeunload');
     // AccSvc.signOut();
     });*/
    //关闭标签页自动注销
    window.close = function (event) {
        // console.debug('window close');
        sessionStorage.setItem('last-close', 'window-close');
        AccSvc.signOut();
    }
}];

/*// debug
 window.document.onready = function () {
 console.debug('last-close=' + sessionStorage.getItem('last-close'));
 // alert(sessionStorage.getItem('last-close'));
 sessionStorage.setItem('last-close', '')
 };*/

//**** 财务人员fw控制器 ----->>>>start *****//

var fwCtrl = ['$scope', '$state', '$timeout', '$uibModal', 'FncRmindService', 'ChkRsltSvc', '$rootScope',
    function (sgop, state, timeout, msgbox, NotifSvc, ChkSvc, rootsgop) {
        // console.debug('fw info ctrl');

        sgop.checkingEnv = function () {
            ChkSvc.initCheckingEnv().then(function (data) {
                $.extend(sgop.lastUpload, data.lastUpload);
                var caid = data.caid;
                var dash1 = caid.indexOf('-');
                var stateParams = {};
                stateParams.year = caid.substring(0, dash1);
                stateParams.month = caid.substring(dash1 + 1, caid.indexOf('-', dash1 + 1));
                state.go('u.fw.p', stateParams);
            }, function (errMsgObj) {
                var boxInst = showMsg({
                    msgbox: msgbox,
                    msgHtml: '对账环境准备收了，网络或系统错误，请重试。'
                });
                timeout(function () {
                    boxInst.dismiss();
                }, msgErrTimeout);
            });
        };

    }];
/*
 // 财务人员导航数据获取
 var fwNavCtrl = ['$scope', '$state', 'OrderService',
 function ($scope, $state, OrderService) {
 console.debug('fwNavCtrl,', $scope);

 }];*/

function ensureErrMsg(errMsgObj) {
    return errMsgObj.msg || errMsgObj.errmsg || '无更详细信息';
}
/*
 // 财务人员账单
 var fwoCtrl = ['$scope', '$state', '$stateParams', 'OrderService', function (sgop, $state, $stateParams, OrderService) {
 console.debug('fwoGridCtrl, params: ', $stateParams);

 // [caution] 变量与Uploadctrl中有重复！！

 var self = this;

 function tryRefreshGrid() {
 sgop.isLoading = true;
 OrderService.orders().then(function (orders) {
 sgop.orders = orders;
 // self.orders = orders;
 // isNonEmptyGrid => 蕴含已获得数据反馈，数据或许空或许不空
 sgop.isNonEmptyGrid = orders.length > 0;
 }, function (errMsgObj) {
 sgop.errMsg = '请求数据失败：' + ensureErrMsg(errMsgObj);
 sgop.canShowError = true;
 }).finally(function () {
 sgop.isLoading = false;
 });
 };
 // init grid
 tryRefreshGrid();

 sgop.$on('EvtUploadOrderSucc', function (evt, newOrders) {
 console.debug('event: EvtUploadOrderSucc caught', evt, newOrders);
 // clear status text
 // evt.stopPropagation();

 clearStatus();

 sgop.isNonEmptyGrid = newOrders.length > 0;
 sgop.orders = newOrders;
 // self.orders = newOrders;
 });

 // 'reset' scope
 function clearStatus() {
 // 都是为加载grid而定义的变量
 sgop.isLoading = undefined;
 sgop.canShowError = undefined;
 sgop.isNonEmptyGrid = undefined;
 sgop.errMsg = '';

 // sgop.orders = undefined;
 }
 }];*/

// 文件上传
var uploadCtrl = ['$scope', 'Upload', '$timeout', '$state', '$rootScope', '$filter', 'ChkRsltSvc',
    function (sgop, Upld, timeout, $state, rootsgop, $filter, ChkSvc) {
        // console.debug('file upload ctrl');

        // for SomeController as ctrl; let var ctrl = this;

        /*        var data = stateParams;
         sgop.lastUpload = {
         time:data.lastUploadTime
         };*/

        /*sgop.uploadFileSelect = function (selectedFile) {
         var info = sgop.uploadInfo;
         try {
         if (selectedFile) {
         info.fileName = selectedFile.name;
         console.debug('uploading file: ', selectedFile, sgop.uploadFileType);
         info.isUploading = true;
         // 使用timeout让控制器有时间先更新UI显示正准备上传的信息
         timeout(function () {
         Upld.upload({
         url: ReqUrl.fwOrderUpload,
         data: {"file": selectedFile, "import_select": sgop.uploadFileType}
         }).then(function (resPkg) {
         console.debug('response recved', resPkg);
         var resbody = resPkg.data;
         if (isOkResBody(resbody)) {
         info.uploadResult = true;
         //todo refresh data <- upload
         console.debug('trying to refresh grid of orders after successful uploading file');

         if (sgop.uploadFileType == 'A') {
         sgop.$emit('EvtUploadOrderSucc', resbody.data);
         }
         } else {
         info.uploadResult = false;
         info.errMsg = '上传失败，' + (resbody.errmsg || '无更详细信息');
         }
         info.isUploading = false;
         }, function (errPkg) {
         console.error('upload err', errPkg);
         info.errMsg = '网络或系统错误';
         info.uploadResult = false;
         }, function (evt) {
         console.debug('evt', evt);
         info.progressMax = parseInt(evt.total);
         info.progress = parseInt(evt.loaded);
         }).catch(function (ex) {
         console.debug('catch exception:', ex);
         info.isUploading = false;
         info.uploadResult = false;
         info.errMsg = '网络或系统错误';
         }).finally(function () {
         info.isUploading = false;
         });
         }, 50);
         } else {
         info.errMsg = '请（正确）选择文件';
         }
         } catch (expt) {
         console.error(expt);
         info.errMsg = '上传失败，网络或系统错误';
         }
         };*/

        var str = sessionStorage.getItem(lastUploadInfoKey);
        rootsgop.lastUpload = rootsgop.lastUpload || {};
        $.extend(rootsgop.lastUpload, str ? JSON.parse(str).lastUpload : {});

        sgop.formSubmit = function () {
            var info = sgop.uploadInfo;
            try {
                //console.debug('上传货款和账单', sgop.uploadCfg);
                info.isUploading = true;
                // 使用timeout让控制器有时间先更新UI显示正准备上传的信息
                timeout(function () {
                    sgop.uploadCfg.caid = Encrypt(rootsgop.caid, true);
                    Upld.upload({
                        url: ReqUrl.fwOrderUpload,
                        data: sgop.uploadCfg
                    }).then(function (resPkg) {
                        console.debug('(upload) response recved', resPkg);
                        var resbody = JSON.parse(Decrypt(resPkg.data));
                        console.debug('decrypted: ', resbody);
                        if (isOkResBody(resbody)) {
                            sgop.lastUpload.time = resbody.lastUploadTime || resbody.data && resbody.data.lastUploadTime || $filter('date')(new Date(), appConf.tmFmtLong);
                            sgop.lastUpload.result = '成功';
                            info.uploadResult = true;
                        } else {
                            sgop.lastUpload.result = '失败';
                            info.uploadResult = false;
                            info.errMsg = '上传失败，' + (resbody.errmsg || '无更详细信息');
                        }
                        sessionStorage.setItem(lastUploadInfoKey, JSON.stringify(sgop.lastUpload));
                        info.isUploading = false;
                    }, function (errPkg) {
                        //console.error('upload err', errPkg);
                        info.errMsg = '网络或系统错误';
                        info.uploadResult = false;
                    }, function (evt) {
                        // console.debug('evt', evt);
                        info.progressMax = parseInt(evt.total);
                        info.progress = parseInt(evt.loaded);
                    }).catch(function (ex) {
                        //console.error(ex);
                        info.isUploading = false;
                        info.uploadResult = false;
                        info.errMsg = '网络或系统错误';
                    }).finally(function () {
                        info.isUploading = false;
                    });
                }, 50);
            } catch (expt) {
                //console.error(expt);
                info.errMsg = '上传失败，网络或系统错误';
            }
        };
    }];

var fwcCtrl = ['$scope', '$stateParams', '$rootScope', function (sgop, stateParams, rootsgop) {
    // console.debug('fwcCtrl->', stateParams);

    sgop.targetDate = stateParams;

    /*    // 禁止浏览器回退到对账流程的上一步
     sgop.$on('$stateChangeStart',
     function (event, toState, toParams, fromState, fromParams) {
     // 可以从最后一步（对账结果）返回到对账流程第一步，即允许“重新对账”
     if (fromState.name.indexOf('u.fw.p') > -1 && toState.name.indexOf('u.fw.p') > -1 && !(fromState.name == 'u.fw.p.m.x' && toState.name == 'u.fw.p') && toState.name.length < fromState.name.length) {
     //console.debug('禁止浏览器回退');
     event.preventDefault();
     window.history.forward();
     }
     });*/

    sgop.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
        // console.debug('state changed: ', fromState, toState);
        sgop.checkProgress = {};
        switch (toState.name) {
            case 'u.fw.p':
                sgop.checkProgress.step = 1;
                break;
            case 'u.fw.p.m':
                sgop.checkProgress.step = 2;
                break;

            // 流程3是对账操作，只是一个对后端的请求，没有数据页面，没有单独的状态，只有一个消息框

            case 'u.fw.p.m.x':
                sgop.checkProgress.step = 4;
                break;

            default:
                sgop.checkProgress.step = -1;
        }
        rootsgop.inChecking = sgop.checkProgress.step > 1 && sgop.checkProgress.step < 4;
    });
}];

// register controller
app.controller('UploadCtrl', uploadCtrl);

function showMsg(msgCfg/*msgbox, title, msgHtml*/) {
    if (arguments.length == 3) {
        msgCfg = {
            msgbox: msgCfg,
            title: arguments[1],
            msgHtml: arguments[2]
        }
    }
    return msgCfg.msgbox.open({
        templateUrl: 'msgbox.html',
        controller: msgCfg.ctrl || ['$scope', '$uibModalInstance', function (sgop, msgbox) {
            // sgop.msgboxTitle = msgCfg.title;
            // sgop.msgHtml = msgCfg.msgHtml;
            sgop.msgCfg = msgCfg;
            sgop.closeMsgbox = function () {
                msgbox.dismiss();
            };
            sgop.okBtnClick = function () {
                msgbox.close();
            };
            sgop.cancelBtnClick = function () {
                msgbox.dismiss();
            };
        }],
        backdrop: msgCfg.backdrop === undefined ? !msgCfg.important : msgCfg.backdrop,
        keyboard: msgCfg.keyboard === undefined ? !msgCfg.important : msgCfg.keyboard,   // if undefined, can close by ESC
        size: msgCfg.msgboxSize || 'md',
    });
}


function initGridStatus(sgop) {
    sgop.canShowError = sgop.isNonEmptyGrid = sgop.isLoading = sgop.items = sgop.errMsg = undefined;
}

function gridPage(sgop, tableState, $filter, businessFnc) {
    if (tableState.pagination.lastStartIdx == tableState.pagination.start && sgop.items) {
        // console.debug('only local search & sort; items, dispItems', sgop.items, sgop.dispItems);
        localSearchAndSort();
        return;
    }

    function localSearchAndSort() {
        var searchCriteria = tableState.search.predicateObject;
        sgop.dispItems = /*angular.copy*/(sgop.items);
        // search
        sgop.dispItems = $filter('rmdsFilter')(sgop.dispItems, searchCriteria);
        // sort
        if (tableState.sort.predicate) {
            sgop.dispItems = $filter('orderBy')(sgop.dispItems, tableState.sort.predicate, tableState.sort.reverse);
        }
    }

    initGridStatus(sgop);

    var pagination = tableState.pagination;

    var start = pagination.start || 0;     // This is NOT the page number, but the index of item in the list that you want to use to display the table.
    var numberPerPage = pagination.number;  // Number of entries showed per page.
    var pageNum = Math.floor(start / numberPerPage) + 1; // first page with 1 not 0

    sgop.isLoading = true;
    businessFnc({
        pagenum: pageNum
        , search: tableState.search.predicateObject
        , sort: {field: tableState.sort.predicate, desc: tableState.sort.reverse}
    }).then(function (data) {

        tableState.pagination.lastStartIdx = start;

        var items = data.items || data.data;

        sgop.items = items;
        sgop.dispItems = items;

        localSearchAndSort();

        var serverTotalItemCount = data.totalItemCount;
        pagination.numberOfPages = serverTotalItemCount && Math.ceil(serverTotalItemCount / pagination.number);
        pagination.numberOfPages = pagination.numberOfPages || data.totalpage;
        pagination.totalItemCount = serverTotalItemCount;
        sgop.isNonEmptyGrid = items.length > 0;
    }, function (err) {
        //console.warn(err);
        sgop.canShowError = true;
        sgop.errMsg = '请求数据失败：' + ensureErrMsg(err);
    }).finally(function () {
        sgop.isLoading = false;
    });
}

// 财务人员“付款通知”（未入对账依赖数据）
var fwnCtrl = ['$scope', 'FncRmindService', 'Lightbox', "$uibModal", '$state', 'ChkRsltSvc', '$timeout', '$filter',
    function (sgop, FncRmindService, picModal, msgBox, state, ChkRsltSvc, timeout, $filter) {
        //console.debug('fwnCtrl 待审付款通知');

        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, FncRmindService.fncReminds);
        };

        function opPaymentNotifyItem(paymentNotifyItem, logtxt, svcFnc) {
            //console.debug(logtxt);
            //console.debug('数据操作，前：', paymentNotifyItem);

            var approveCfg = {
                "id": paymentNotifyItem.id
            };
            var msgcfg = {msgbox: msgBox, title: '审核通过操作', msgHtml: '操作<strong class="">正在进行……</strong>'};
            var boxInst = showMsg(msgcfg);
            svcFnc(approveCfg).then(function (status) {
                paymentNotifyItem.result = status;
                msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                timeout(function () {
                    boxInst.dismiss();
                }, msgOkTimeout)
            }, function (errMsgObj) {
                //console.debug(logtxt + '失败');
                msgcfg.msgHtml = '操作<strong class="text-info">失败</strong>';
                timeout(function () {
                    boxInst.dismiss();
                }, msgErrTimeout)
            }).finally(function () {
            });
        }


        //  // "通过"付款通知和出纳
        // sgop.approvePaymentNotification = function (paymentNotifyItem) {
        //     opPaymentNotifyItem(paymentNotifyItem, "“通过”（关联付款通知和出纳）", FncRmindService.approvePaymentNotification)
        // };

        // 否决付款通知（标记为无效的用户上传数据）
        sgop.rejectPaymentNotification = function (paymentNotifyItem) {
            opPaymentNotifyItem(paymentNotifyItem, '“否决”（取消付款通知与出纳的关联）', FncRmindService.rejectPaymentNotification)
        };

        // “待定”
        sgop.tbdPaymentNotification = function (paymentNotifyItem) {
            opPaymentNotifyItem(paymentNotifyItem, '“待定”（）', FncRmindService.tbdPaymentNotification);
        };

        // 关联到出纳
        sgop.attachPaymentNotification = function (paymentNotifyItem) {
            msgBox.open({
                templateUrl: 'attach-to-t.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    var ctrl = this;
                    ctrl.r = paymentNotifyItem;
                    (function getCandidates() {
                        ctrl.isLoading = true;
                        FncRmindService.getBankTransactionCandidates({id: paymentNotifyItem.id})
                            .then(function (candidates) {
                                ctrl.isNonEmptyGrid = candidates.length > 0;
                                ctrl.trans = candidates;
                                if (!paymentNotifyItem.matchedId && candidates.length > 0) {
                                    ctrl.attachCfg.targetId = candidates[0].id;
                                }
                            }, function (errMsgObj) {
                                //console.debug(errMsgObj);
                                ctrl.canShowError = true;
                                ctrl.errMsg = ensureErrMsg(errMsgObj);
                            })
                            .catch(function (excpt) {
                                //console.error(excpt);
                            })
                            .finally(function () {
                                ctrl.isLoading = false;
                            });
                    }());

                    sgop.attachSubmit = function () {
                        ctrl.isProcessing = true;
                        ctrl.attachErrMsg = undefined;
                        FncRmindService.attachToBankTransaction(ctrl.attachCfg)
                            .then(function (res) {
                                // 显示为“已通过”
                                paymentNotifyItem.result = 'Y';
                                msgbox.close();
                            }, function (errMsgObj) {
                                ctrl.attachErrMsg = errMsgObj.msg;
                            })
                            .finally(function () {
                                ctrl.isProcessing = false;
                            });
                    };

                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg',
                controllerAs: 'ctrl'

            }).result.then(function (ok) {
                var boxInst = showMsg({
                    msgbox: msgBox,
                    title: '匹配付款通知到出纳',
                    msgHtml: '匹配<strong class="text-success">成功</strong>'
                });
                timeout(function () {
                    boxInst.dismiss();
                }, msgOkTimeout);
            });
        };

        // 显示凭证图片
        sgop.showPicture = function (url) {
            // console.debug('pic url: ', url);
            // picModal.open({
            //     templateUrl: 'showPicTpl.html',
            //     controller: ['$scope','$uibModalInstance', function (sgop,picModal) {
            //         sgop.imgUrl = url;
            //         sgop.closePicModal=function () {
            //             picModal.dismiss();
            //         };
            //     }],
            //     // size:'220px × 117',
            //     resovle: {
            //         // "imgUrl": function () {
            //         //     return url;
            //         // }
            //     },
            // });
            picModal.openModal([url], 0);
        };

        // 多合同的付款分配详细信息
        sgop.showCM = function (item) {
            msgBox.open({
                templateUrl: 'cm-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.r = item;

                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }]
            });
        };


        // 对账操作 lol...
        sgop.checkCashing = function () {
            //console.debug('对账操作请求发起');
            sgop.checkProgress.step = 3;
            var msgCfg = {
                msgbox: msgBox,
                title: '对账操作',
                msgHtml: '对账<strong class="text-info">进行中</strong>……',
                noCloseBtn: true,
                ctrl: ['$scope', '$uibModalInstance', '$timeout', function (sgop, boxInst, timeout) {
                    sgop.msgCfg = msgCfg;
                    sgop.closeMsgbox = function () {
                        boxInst.dismiss();
                    }
                }],
                backdrop: 'static',
                keyboard: false,
            };

            var boxInst = showMsg(msgCfg);
            ChkRsltSvc.checkWork()
                .then(function () {
                    msgCfg.msgHtml = '对账<strong class="text-success">成功</strong>，正跳转到对账结果展示页面……';
                    timeout(function () {
                        boxInst.dismiss();
                        state.go(".x");
                    }, 800);
                }, function (errMsgObj) {
                    // 窗口已经打开，再改动以下两个参数已无作用
                    // msgCfg.keyboard = true;
                    // msgCfg.backdrop = undefined;

                    msgCfg.noCloseBtn = false;
                    msgCfg.msgHtml = '对账<strong class="text-danger">失败</strong>，错误：' + errMsgObj.msg;
                    // 关闭信息窗口后，将对账流程显示回退到第2步
                    boxInst.result.finally(function () {
                        sgop.checkProgress.step = 2;
                    });
                })
                .finally(function () {
                });
        };


        // /**
        //  * 将状态（付款通知审核结果）的后台存储码映射对用户友好的、给用户展示的文本
        //  * @param statusCode
        //  * @returns {*}
        //  */
        // sgop.translateNotifyStatus = function translateNotifyStatus(statusCode) {
        //     switch (statusCode) {
        //         case 'Y':
        //             return '已通过';
        //         case 'N':
        //             return '被否决';
        //         case 'W':
        //             return '待定中';
        //         case 'M':
        //             return '已通过';
        //         case 'V':
        //             return '无';
        //         default:
        //             return '';
        //     }
        // };

    }];

function getRefreshFnc(sgop, svcfnc, tab) {
    return function refreshGrid() {
        sgop.$watch('initTab', function (nv, ov) {
            if (nv == tab) {
                // console.debug('initing tab-' + tab);
                initGridStatus(sgop);
                sgop.isLoading = true;
                svcfnc()
                    .then(function (rows) {
                        sgop.isNonEmptyGrid = rows.length > 0;
                        sgop.rows = rows;

                        sgop.tabInited[tab] = true;
                    }, function (errMsgObj) {
                        sgop.canShowError = true;
                        sgop.errMsg = errMsgObj.msg;
                    })
                    .finally(function () {
                        sgop.isLoading = false;
                    });
            }
        });
    };
}

// 预览付款通知数据
var fwvCtrl = ['$scope', 'Lightbox', '$uibModal', 'FncRmindService', '$filter', function (sgop, picModal, msgBox, NotifSvc, $filter) {
    //console.debug('fwv ctrl->预览付款通知数据');

    sgop.checkInoperable = true;
    sgop.refreshGrid = function (tableState) {
        // console.debug('table refresh with state:', tableState);
        gridPage(sgop, tableState, $filter, NotifSvc.viewNotifications);
    };
    // sgop.refreshGrid({pagination: {}});


    // 多合同的付款分配详细信息
    sgop.showCM = function (item) {
        msgBox.open({
            templateUrl: 'cm-detail-box.html',
            controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                sgop.r = item;

                sgop.closeMsgbox = function () {
                    msgbox.dismiss();
                };
            }]
        });
    };
}];

// 历史对账结果查看
var fwhCtrl = ['$scope', 'ChkRsltSvc', '$uibModal', '$state', function (sgop, ChkRsltSvc, msgbox, state) {
    //console.debug('ctrl->历史对账结果');

    var thisYear = new Date().getFullYear();
    sgop.yearInput = sgop.maxYear = thisYear;
    sgop.minYear = 2000;

    sgop.formSubmit = function () {
        //console.debug('查询' + sgop.yearInput + '年对账历史');
        if (sgop.yearInput) {
            refreshData();
        }
    };

    // 立即显示本年度已有对账结果
    sgop.formSubmit();

    function refreshData() {
        sgop.isLoading = true;
        sgop.canShowError = undefined;
        sgop.isNonEmptyGrid = undefined;
        sgop.year = sgop.yearInput;
        ChkRsltSvc.historyResults(sgop.year)
            .then(function (yearHistory) {
                sgop.isNonEmptyGrid = true;
                sgop.historyResults = yearHistory;
            }, function (errMsgObj) {
                sgop.canShowError = true;
                sgop.errMsg = errMsgObj.msg;
            })
            .finally(function () {
                sgop.isLoading = false;
            });
    };

    sgop.recheck = function (year, month, noConfirm) {
        if (noConfirm) {
            var opInfo = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                , size: 'sm'
                , important: true
            };
            var boxInst = showMsg(opInfo);
            ChkRsltSvc.recheck2(year, month).then(function (ok) {
                boxInst.close();
                state.go('u.fw.p', {year: year, month: month});
            }, function (errObj) {
                opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
            });
        } else {
            var msgConfirm = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '确定丢弃 <strong>' + year + '年' + month + '月</strong> 的对账结果,并重新核对当月账目吗?'
                , okBtn: true
                , cancelBtn: true
                , important: true
            };
            showMsg(msgConfirm).result.then(function () {
                var opInfo = {
                    msgbox: msgbox
                    , title: '重新对账操作'
                    , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                    , size: 'sm'
                    , important: true
                };
                var boxInst = showMsg(opInfo);
                ChkRsltSvc.recheck2(year, month).then(function (ok) {
                    boxInst.close();
                    state.go('u.fw.p', {year: year, month: month});
                }, function (errObj) {
                    opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
                });
            });
        }
    };

    sgop.thisDate = new Date();

}];

// 对账结果
var fwxCtrl = ['$scope', 'ChkRsltSvc', '$uibModal', '$state', '$stateParams', function (sgop, ChkRsltSvc, msgbox, state, stateParams) {
        //console.debug('fwxCtrl 报表标签组');

        var self = this;

        sgop.tabInited = [];
        // sgop.tabCfg={
        //     openTab:0
        // };

        sgop.sel = function (tab) {
            if (!sgop.tabInited[tab]) {
                // console.debug('Tab init event of tab-' + tab + ' fired');
                sgop.initTab = tab;
                // ('initTab' + tab);
            }
            // sgop.tabCfg.openTab = tab;
        };

        // 到处对账结果
        ChkRsltSvc.checkResultExport().then(function (fileUrl) {
            sgop.fileExportedUrl = fileUrl;
        }, function (errMsgObj) {
            sgop.exportErr = '报表生成失败：' + errMsgObj.msg;
        });

        // 重新对账
        sgop.startOver = function () {
            // var msgCfg = {
            //     msgbox: msgbox,
            //     title: '请求重新对账',
            //     msgHtml: '重新对账操作请求中……',
            //     noCloseBtn: true
            // };
            // var boxInst = showMsg(msgCfg);
            // // “重新对账”请求成功
            // ChkRsltSvc.reCheck().then(function () {
            //     boxInst.close();
            //     state.go('u.fw.p');
            // }, function (errMsgObj) {
            //     msgCfg.msgHtml = '操作请求失败：' + ensureErrMsg(errMsgObj);
            // });
            var year = stateParams.year;
            var month = stateParams.month;
            var msgConfirm = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '确定丢弃 <strong>' + year + '年' + month + '月</strong> 的对账结果,并重新核对当月账目吗?'
                , okBtn: true
                , cancelBtn: true
                , important: true
            };
            showMsg(msgConfirm).result.then(function () {
                var opInfo = {
                    msgbox: msgbox
                    , title: '重新对账操作'
                    , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                    , size: 'sm'
                    , important: true
                };
                var boxInst = showMsg(opInfo);
                ChkRsltSvc.reCheck().then(function (ok) {
                    boxInst.close();
                    state.go('u.fw.p');
                }, function (errObj) {
                    opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
                });
            });
        };

// “返利”
        sgop.acceptResult = function () {
            var msgCfg = {
                msgbox: msgbox,
                title: '返利操作',
                msgHtml: '操作<strong class="text-info">执行中</strong>……',
                noCloseBtn: true
            };
            var boxInst = showMsg(msgCfg);
            // “重新对账”请求成功
            ChkRsltSvc.acceptCheckingResult().then(function () {
                msgCfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                setTimeout(function () {
                    boxInst.close();
                }, msgOkTimeout);
                boxInst.result.finally(function () {
                    sgop.resultChkd = true;
                    // state.go('u.fw.p');
                });
            }, function (errMsgObj) {
                msgCfg.msgHtml = '操作<strong class="text-danger">失败</strong>：' + ensureErrMsg(errMsgObj);
            }).finally(function () {
                msgCfg.noCloseBtn = false;
            });
        };
    }]
    ;


// 对账结果分类显示 X1->X8
app.controller('X1Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无匹配合同、客户的出纳'
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);


    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToNeitherContractNorClient, 1)();

}]);

app.controller('X2Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '未到账的货款'
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.ordersUnpaid, 2)();

}]);

app.controller('X3Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无匹配出纳的付款通知'
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationNotAttachedToTransaction, 3)();

}]);

app.controller('X4Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '有匹配合同的出纳',
        connectType: 'A',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'contract-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToContract, 4)();
}]);

app.controller('X5Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '有匹配客户的出纳',
        connectType: 'C'
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToClient, 5)();
}]);

app.controller('X6Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '有出纳的付款通知',
        connectType: 'H',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'trans-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationAttachedToTransaction, 6)();
}]);

app.controller('X7Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '已到账的货款',
        connectType: 'T',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'trans-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.ordersPaid, 7)();
}]);

app.controller('X8Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无效的付款通知'
    };

    //console.debug('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationInvalid, 8)();
}]);


// 管理员
var fsCtrl = ['$scope', function (sgop) {
    //console.debug('fsCtrl');
}];

function refreshGridFnc(svcfnc, sgop) {
    return function () {
        sgop.isLoading = true;
        svcfnc().then(function (items) {
            // console.debug('recvd:',items)
            sgop.items = items;
        }, function (errobj) {
            //console.error(errobj);
            sgop.errMsg = ensureErrMsg(errobj);
        }).finally(function () {
            sgop.isLoading = false;
        });
    }
}

function approveReg(item, itemsIgnored, svcfnc, msgbox, itemsScope) {
    var msgcfg = {
        msgbox: msgbox
        , title: '审阅操作结果'
        , msgHtml: '操作<strong class="text-info">执行中</strong>'
        , size: 'sm'
    };
    var boxInst = showMsg(msgcfg);
    svcfnc(item.workId || item.username).then(function (ok) {
        msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
        setTimeout(function () {
            boxInst.close();
        }, msgOkTimeout);
        // 如果用户注册审阅操作成功（被通过、被拒绝），则不再在表中显示
        var items = itemsScope.dispItems;
        var i = items.indexOf(item);
        if (i > -1) {
            items.splice(i, 1);
        }
    }, function (errobj) {
        msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>，' + ensureErrMsg(errobj);
        setTimeout(function () {
            boxInst.close();
        }, msgErrTimeout);
    }).finally(function () {
    });
};

// 审阅对账联系人注册申请
var fsanCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        // console.debug('fsan Ctrl->审阅对账联系人注册申请');

        // refreshGridFnc(AccSvc.regPendingPaymentNotifiers, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.regPendingPaymentNotifiers);
        };
        var items = sgop.dispItems;

        sgop.acceptNotifier = function (item) {
            approveReg(item, items, AccSvc.acceptNotifierReg, msgbox, sgop);
        };
        sgop.rejectNotifier = function (item) {
            approveReg(item, items, AccSvc.rejectNotifierReg, msgbox, sgop);
        };
    }];
app.controller('fsanCtrl', fsanCtrl);

// 审阅代理商注册
var fsawCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        //console.debug('fsaw Ctrl->审阅代理商注册');

        // refreshGridFnc(AccSvc.regPendingFinancialWorker, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.regPendingFinancialWorker);
        };
        // sgop.isLoading = true;
        // AccSvc.regPendingFinancialWorker().then(function (items) {
        //     sgop.items = items;
        // }, function (errobj) {
        //     console.error(errobj);
        //     sgop.errMsg = ensureErrMsg(errobj);
        // }).finally(function () {
        //     sgop.isLoading = false;
        // });
        var items = sgop.dispItems;
        sgop.accept = function (item) {
            // approveReg(item, sgop.items, AccSvc.acceptFinancialWorkerReg, msgbox, sgop);
            approveReg(item, items, AccSvc.acceptFinancialWorkerReg, msgbox, sgop);
        };
        sgop.reject = function (item) {
            approveReg(item, items, AccSvc.rejectFinancialWorkerReg, msgbox, sgop);
        };
    }];
app.controller('fsawCtrl', fsawCtrl);

var fsamCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        //console.debug('fsaw Ctrl->审阅代理商注册');

        // refreshGridFnc(AccSvc.regPendingFinancialWorker, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.regPendingFinancialAdmins);
        };
        var items = sgop.dispItems;
        sgop.accept = function (item) {
            approveReg(item, items, AccSvc.acceptFinancialAdminReg, msgbox, sgop);
        };
        sgop.reject = function (item) {
            approveReg(item, items, AccSvc.rejectFinancialAdminReg, msgbox, sgop);
        };
    }];
app.controller('fsamCtrl', fsamCtrl);

function ctrlUser(item, svcfnc, msgbox) {
    var msgcfg = {
        msgbox: msgbox
        , title: '控制操作结果'
        , msgHtml: '操作<strong class="text-info">执行中</strong>'
        , size: 'sm'
    };
    var boxInst = showMsg(msgcfg);
    svcfnc(item.workId || item.username).then(function (newCtrlFlag) {
        msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
        setTimeout(function () {
            boxInst.close();
        }, msgOkTimeout);
        // 改变控制状态
        if (newCtrlFlag) {
            item.ctlflag = newCtrlFlag;
        } else {
            switch (item.ctlflag) {
                case 0:
                    item.ctlflag = -3;
                    break;
                case -3:
                    item.ctlflag = 0;
                    break;
                default:
                    console.warn('should NOT reach here');
                    item.ctlflag = undefined;
            }
        }

    }, function (errobj) {
        msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>，' + ensureErrMsg(errobj);
        setTimeout(function () {
            boxInst.close();
        }, msgErrTimeout);
    }).finally(function () {
    });
}

var fscnCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        //console.debug('fscwCtrl ->控制对账联系人');

        // refreshGridFnc(AccSvc.paymentNotifiers, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.paymentNotifiers);
        };

        sgop.lock = function (item) {
            ctrlUser(item, AccSvc.lockNotifier, msgbox);
        };
        sgop.unlock = function (item) {
            ctrlUser(item, AccSvc.unlockNotifier, msgbox);
        };
    }];
app.controller('fscnCtrl', fscnCtrl);

var fscwCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        //console.debug('ctrl ->');

        // refreshGridFnc(AccSvc.financialWorkers, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.financialWorkers);
        };

        sgop.lock = function (item) {
            ctrlUser(item, AccSvc.lockFworker, msgbox);
        };
        sgop.unlock = function (item) {
            ctrlUser(item, AccSvc.unlockFworker, msgbox);
        };
    }];
app.controller('fscwCtrl', fscwCtrl);

var fscmCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', '$filter',
    function (sgop, timeout, AccSvc, msgbox, $filter) {
        //console.debug('fscmCtrl ->控制代理商管理员');

        // refreshGridFnc(AccSvc.financialWorkers, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, AccSvc.financialAdmins);
        };

        sgop.lock = function (item) {
            ctrlUser(item, AccSvc.lockFAdmin, msgbox);
        };
        sgop.unlock = function (item) {
            ctrlUser(item, AccSvc.unlockFAdmin, msgbox);
        };
    }];
app.controller('fscmCtrl', fscmCtrl);

var fsdCtrl = ['$scope', '$timeout', 'MgmtSvc', '$uibModal', function (sgop, timeout, MgmtSvc, msgbox) {
    sgop.backupDB = function () {
        var msgcfg = {
            msgbox: msgbox,
            title: '备份数据库操作'
            , msgHtml: '操作<strong class="text-info">执行中</strong>'
            , size: 'sm'
        };
        var boxInst = showMsg(msgcfg);
        MgmtSvc.backupDB().then(function (ok) {
            msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
            timeout(function () {
                boxInst.close();
            }, msgOkTimeout);
        }, function (fail) {
            msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>';
            timeout(function () {
                boxInst.close();
            }, msgErrTimeout);
        });
    };

    sgop.restorePoints = refreshGridFnc(MgmtSvc.getDBBackups, sgop);

    sgop.restoreDB = function (item) {
        var msgConfirm = {
            msgbox: msgbox
            , title: '恢复数据库操作'
            , msgHtml: '确定将数据库的数据恢复到 <strong class="text-danger">' + item.timeStr + '</strong> 时间点吗?'
            , okBtn: true
            , cancelBtn: true
            , important: true
        };
        showMsg(msgConfirm).result.then(function () {
            var msgcfg = {
                msgbox: msgbox,
                title: '恢复数据库操作'
                , msgHtml: '操作<strong class="text-info">执行中</strong>'
                , size: 'sm'
            };
            var boxInst = showMsg(msgcfg);
            MgmtSvc.restoreDB(item.id).then(function (ok) {
                msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                timeout(function () {
                    boxInst.close();
                }, msgOkTimeout);
            }, function (fail) {
                msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>';
                timeout(function () {
                    boxInst.close();
                }, msgErrTimeout);
            });
        });
    };
}];

// 用户日志
var fslCtrl = ['$scope', '$timeout', 'MgmtSvc', '$uibModal', '$filter',
    function (sgop, timeout, MgmtSvc, msgbox, $filter) {
        //console.debug('fslCtrl->查看用户操作日志');
        // refreshGridFnc(MgmtSvc.fetchLogs, sgop)();
        sgop.refreshGrid = function (tableState) {
            // console.debug('table refresh with state:', tableState);
            gridPage(sgop, tableState, $filter, MgmtSvc.fetchLogs);
        };
    }];


// directives
app.directive('onFinishRenderEvent', ['$timeout', function (timeout) {
    return {
        restict: 'A'
        , link: function (scope, ele, attr) {
            if (scope.$last === true) {
                timeout(function () {
                    scope.$emit(attr.onFinishRenderEvent);
                });
            }
        }
    }
}])
    .directive('stDateRange', ['$timeout', function ($timeout) {
        return {
            restrict: 'E',
            require: '^stTable',
            scope: {
                before: '=',
                after: '='
            },
            templateUrl: 'stDateRange.html',

            link: function (scope, element, attr, table) {

                function dateChange() {
                    trySearch();
                };
                scope.afterDateChange = scope.beforeDateChange = dateChange;

                scope.afterOpt = {
                    showWeeks: false
                    , maxDate: new Date()
                };
                scope.beforeOpt = {
                    showWeeks: false
                };

                var inputs = element.find('input');
                var inputBefore = ng.element(inputs[0]);
                var inputAfter = ng.element(inputs[1]);
                var predicateName = attr.predicate;

                var trySearch = function () {
                    // console.debug('try search table with new date range; before, after', scope.before, scope.after);
                    var query = {};
                    // if (!scope.isBeforeOpen && !scope.isAfterOpen) {
                    // magic condition…… this indicates the closing of datepicker popup dialog
                    // if (scope.isBeforeOpen ^ scope.isAfterOpen) {
                    $timeout(function () {
                        if (scope.before) {
                            query.before = scope.before;
                            scope.afterOpt.maxDate = query.before;
                        }
                        if (scope.after) {
                            query.after = scope.after;
                            scope.beforeOpt.minDate = scope.after;
                        }
                        scope.$apply(function () {
                            // console.debug('q, p', query, predicateName);
                            table.search(query, predicateName);
                        })
                    });
                    // }
                };

                /*[inputBefore, inputAfter].forEach(function (input) {
                 //没有效果
                 input.bind('change', trySearch);
                 });*/

                function toggle(indicator, another) {
                    return function ($event) {
                        $event.preventDefault();
                        $event.stopPropagation();

                        if (!indicator) {
                            another = false;
                        }
                        indicator = !indicator;
                    }
                }

                scope.openBefore = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    // if open, close another first
                    if (!scope.isBeforeOpen) {
                        scope.isAfterOpen = false;
                    }
                    scope.isBeforeOpen = !scope.isBeforeOpen;
                };
                scope.openAfter = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    // if open, close another first
                    if (!scope.isAfterOpen) {
                        scope.isBeforeOpen = false;
                    }
                    scope.isAfterOpen = !scope.isAfterOpen;
                };
            }
        }
    }])
    .directive('stNumberRange', ['$timeout', function ($timeout) {
        return {
            restrict: 'E',
            require: '^stTable',
            scope: {
                lower: '=',
                higher: '='
            },
            templateUrl: 'stNumberRange.html',
            link: function (scope, element, attr, table) {
                var inputs = element.find('input');
                var inputLower = ng.element(inputs[0]);
                var inputHigher = ng.element(inputs[1]);
                var predicateName = attr.predicate;

                [inputLower, inputHigher].forEach(function (input, index) {

                    input.bind('blur', function () {
                        var query = {};

                        if (scope.lower) {
                            query.lower = scope.lower;
                        }

                        if (scope.higher) {
                            query.higher = scope.higher;
                        }

                        scope.$apply(function () {
                            // console.debug('q, p', query, predicateName)
                            table.search(query, predicateName)
                        });
                    });
                });
            }
        };
    }])

;
// capture links with [rel='lightbox]
app.run(['$rootScope', function (rootsgop) {
    rootsgop.$on('fwcnGridDone', function () {
        if (!/android|iphone|ipod|series60|symbian|windows ce|blackberry/i.test(navigator.userAgent)) {
            jQuery(function ($) {
                $("a[rel^='lightbox']").picbox({/* Put custom options here */}, null, function (el) {
                    return (this == el) || ((this.rel.length > 8) && (this.rel == el.rel));
                });
            });
        }
    });
}]);