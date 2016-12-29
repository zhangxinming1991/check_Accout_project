"use strict";

var app = angular.module('sany', ['ui.router', 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'smart-table', 'ngCookies',
    'ui.router.stateHelper', 'ngFileUpload', 'bootstrapLightbox', 'angular-ladda']);

// 修改后端请求URL
var ReqUrl = {
    // 登陆
    signIn: '/PMController/login',
    // 注册
    signUp: '/PMController/as_register',
    // 注销（登出）
    signOut: '/PMController/signout'
    //找回密码->发送验证码
    , rstPwdSendvc: '/PMController/getresetpwdverifycode'
    //提交找回密码申请
    , rstPwd: '/PMController/forgetandsendmail',
    // 修改个人信息
    updateUserInfo: '/PMController/modifyAssistanceMes',

    // 财务人员->订单
    //   fwOrders:'/Check_MainController/upload_success',
    fwOrders: '/Check_MainController/Watch',
    // fwOrderMonths: '/fw/ordermonths',
    // 财务人员 上传
    fwOrderUpload: '/Check_MainController/upload',
    // 增量上传出纳表
    fwOrderIncrUpload: '/Check_MainController/uploadBinput_incre',
    //财务人员 -> 系统无法关联的付款通知（用户上传的数数据）
    fwFncReminds: '/Check_MainController/Watch',
    // fwFncBadReminds:'fw/fncBadReminds',
    // 财务人员->未关联订单的出纳记录
    fwFncBankTrans: '/Check_MainController/Watch',
    //关联出纳到订单或客户
    fwAssocTrans: '/Check_MainController/Modify',
    // 审核通过付款通知（用户上传数据）
    fwPaymentNotifApprov: '/Check_MainController/Check',
    //关联付款通知到出纳信息
    attachToTrans: '/Check_MainController/Map',
    //待关联付款通知的出纳猴候选集
    attachToTransCandidates: '/Check_MainController/Map',
    // 对账操作
    checkWork: '/Check_MainController/Start_CheckA_Work',
    // 查看对账结果
    checkResult: '/Check_MainController/Watch_CheckA_Result',
    // 导出对账结果报表
    checkResultExport: '/Check_MainController/Export_CheckA_Result',
    // 历史对账结果查询
    historyResults: '/Check_MainController/Watch',
    // 重新对账
    reCheck: '/Check_MainController/ClAndStAgain_CheckA'
    // 历史对账结果中的重新对账操作
    , recheck2: '/Check_MainController/historyca'
    // 认可对账结果操作
    , accChkRlt: '/Check_MainController/freeback'
    // // 新付款通知信息（用户上传）数目
    // newNotifNum: '/Check_MainController/goto_main',
    // “预览”付款通知数据（用户上传）
    , notifView: '/Check_MainController/Watch'
    // 准备对账的数据环境（进入对账模式申请）
    , prepareChkEnv: '/Check_MainController/enter_camodel'

    // 注册申请中的对账联系人列表获取
    , regPendingNotifiers: '/PMController/watch'
    // 注册申请中的代理商列表获取
    , regPendingFworkers: '/PMController/watch'
    // 注册申请中的代理商管理员列表获取
    , regPendingFAdmins: '/PMController/watch'
    // 审阅对账联系人注册
    , approveNotifier: '/PMController/verify_register'
    // 审阅代理商注册
    , approveFw: '/PMController/verify_register'
    // 审阅代理商管理员注册
    , approveFAdmin: '/PMController/verify_register'
    // 已注册的对账联系人
    , notifiers: '/PMController/watch'
    // 已注册代理商财务员
    , fworkers: '/PMController/watch'
    // 已注册的代理商方面的管理员列表
    , fadmins: '/PMController/watch'
    // 锁定、解锁联系人
    , ctrlNotifier: '/PMController/control_power'
    // 锁定、解锁代理商财务员
    , ctrlFworker: '/PMController/control_power'
    // 锁定、解锁代理商管理员
    , ctrlFAdmin: '/PMController/control_power'

    // 备份数据库操作
    , backupdb: '/PMController/backupdb'
    // 恢复数据库
    , restoredb: '/PMController/verify_backup'
    // 已备份数据库列表
    , dbbackups: '/PMController/choose_backup'
    // 日志获取
    , viewLog: '/PMController/watch'
    // 获取代理商列表
    , fetchAgents: '/PMController/get_agentcodeAname'

    // 积分模块
    // 超级管理员获取所有用户积分信息
    , scoreInAllAgents: '/ScoreController/all_scoreinfos'
    // 财务员获取所在代理商管理的用户的积分信息
    , scoreInAgent: '/ScoreController/agent_scoreinfos'
    // 超管、财务 -> 用户积分详情
    , scoreDetail: '/ScoreController/score_records'
    // 超级管理员 积分管理表
    , scoreMgmtAll: '/ScoreController/manage_exchange'
    // 财务员 积分管理表
    , scoreMgmtInAgent: '/ScoreController/agent_exchangeinfos'
    // 超级管理员↓
    // 导出积分报表
    , exportScoreTbl: '/ScoreController/download_scoreinfo'
    // 导出积分兑换报表
    , exportScoreExchgTbl: '/ScoreController/download_exchangeinfo'
    // 确认 积分兑换
    , approveScoreExchg: '/ScoreController/approval_exchange'
    // 下载礼品信息报表
    , exportGiftCat: '/ScoreController/download_giftinfo'
    // 物流详情
    , shipDetail: '/ScoreController/logistic_info'
    // 上传物流
    , uploadShipInfo: '/ScoreController/upload_logistic'
    // 上传礼品列表
    , uploadGiftCat: '/ScoreController/upload_gift'
};

var prefix = '/check_Accout';
(function configBackendUrl(backendReqPrefix) {
    for (var k in ReqUrl) {
        if (ReqUrl.hasOwnProperty(k))
            ReqUrl[k] = backendReqPrefix + ReqUrl[k];
    }
})(prefix);


// string format
String.prototype.format = function () {
    var args = arguments;
    return this.replace(/\{(\d+)\}/g,
        function (m, i) {
            return args[i];
        });
};
String.prototype.contains = function (substr) {
    return this.indexOf(substr) !== -1;
};
String.prototype.startsWith = function (prefix) {
    return this.indexOf(prefix) === 0;
};
/*String.prototype.endsWith=function (postfix) {

 };*/
//static
String.format = function () {
    if (arguments.length == 0)
        return null;

    var str = arguments[0];
    for (var i = 1; i < arguments.length; i++) {
        var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
        str = str.replace(re, arguments[i]);
    }
    return str;
};

function isChinese(str) {
    return str.match(/^[\u4E00-\u9FFF\u3400-\u4DFF\uF900-\uFAFF]+$/g);
}

var appConf = {
    //关账时间，月初5号
    checkClosingDate: 5,
    tmFmtLong: 'yyyy-MM-dd HH:mm:ss',
    tmFmtLongMoment: 'YYYY-MM-DD HH:mm:ss',
    tmFmtYMD: 'yyyy-MM-dd',
    tmFmtYMDMoment: 'YYYY-MM-DD',
    // numberPerPage: 10 ,
    opLogResultTypes: ['成功', '失败'],
    opLogUserRoles: ['客户', '管理员', '代理商财务', '代理商管理'],
    userRegisterWays: ['个体户', '公司'],
    payWays: ['现金', '银行转账', '电汇'],
    scoreStatusInTable: ['兑换中', '正常'],
    // 兑换类型
    exchangeTypes: ['红包', '礼品'],
    regEmailDomainRestrict: ['@sanygroup.com'],
    mappings: {
        scoreStatus: {
            '0': '已提交'
            , '1': '兑换中'
            , '2': '兑换成功'
            , '-1': '兑换失败'
        },
        webUserRole: {
            'bu': '代理商财务',
            'ba': '代理商管理员',
            'bm': '总部管理员',
        },
        /*webUserRoleShort:{
         'bu':'财务',
         'ba':'',
         'bm':'',
         },*/
    },

};

var frontBackEndMappping = {
    userRole: {
        //三一管理员，
        'M': 'bm'
        //代理商财务
        , 'U': 'bu'
        , 'Z': 'ba'
    }
};

// keys
var lastUploadInfoKey = 'last-upload';