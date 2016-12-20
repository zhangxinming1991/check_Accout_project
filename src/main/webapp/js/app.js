"use strict";

var app = angular.module('sany', ['ui.router', 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'smart-table', 'ngCookies',
    'ui.router.stateHelper', 'ngFileUpload', 'bootstrapLightbox', 'angular-ladda']);

// 修改后端请求URL
var ReqUrl = {
    // 登陆
    signIn: '/check_Accout/PMController/login',
    // 注册
    signUp: '/check_Accout/PMController/as_register',
    // 注销（登出）
    signOut: '/check_Accout/PMController/signout'
    //找回密码->发送验证码
    , rstPwdSendvc: '/check_Accout/PMController/getresetpwdverifycode'
    //提交找回密码申请
    , rstPwd: '/check_Accout/PMController/forgetandsendmail'

    // 财务人员->订单
    //   fwOrders:'/check_Accout/Check_MainController/upload_success',
    , fwOrders: '/check_Accout/Check_MainController/Watch',
    // fwOrderMonths: '/fw/ordermonths',
    // 财务人员 上传
    fwOrderUpload: '/check_Accout/Check_MainController/upload',
    // 增量上传出纳表
    fwOrderIncrUpload: '/check_Accout/Check_MainController/uploadBinput_incre',
    //财务人员 -> 系统无法关联的付款通知（用户上传的数数据）
    fwFncReminds: '/check_Accout/Check_MainController/Watch',
    // fwFncBadReminds:'fw/fncBadReminds',
    // 财务人员->未关联订单的出纳记录
    fwFncBankTrans: '/check_Accout/Check_MainController/Watch',
    //关联出纳到订单或客户
    fwAssocTrans: '/check_Accout/Check_MainController/Modify',
    // 审核通过付款通知（用户上传数据）
    fwPaymentNotifApprov: '/check_Accout/Check_MainController/Check',
    //关联付款通知到出纳信息
    attachToTrans: '/check_Accout/Check_MainController/Map',
    //待关联付款通知的出纳猴候选集
    attachToTransCandidates: '/check_Accout/Check_MainController/Map',
    // 对账操作
    checkWork: '/check_Accout/Check_MainController/Start_CheckA_Work',
    // 查看对账结果
    checkResult: '/check_Accout/Check_MainController/Watch_CheckA_Result',
    // 导出对账结果报表
    checkResultExport: '/check_Accout/Check_MainController/Export_CheckA_Result',
    // 历史对账结果查询
    historyResults: '/check_Accout/Check_MainController/Watch',
    // 重新对账
    reCheck: '/check_Accout/Check_MainController/ClAndStAgain_CheckA'
    // 历史对账结果中的重新对账操作
    , recheck2: '/check_Accout/Check_MainController/historyca'
    // 认可对账结果操作
    , accChkRlt: '/check_Accout/Check_MainController/freeback'
    // // 新付款通知信息（用户上传）数目
    // newNotifNum: '/check_Accout/Check_MainController/goto_main',
    // “预览”付款通知数据（用户上传）
    , notifView: '/check_Accout/Check_MainController/Watch'
    // 准备对账的数据环境（进入对账模式申请）
    , prepareChkEnv: '/check_Accout/Check_MainController/enter_camodel'

    // 注册申请中的对账联系人列表获取
    , regPendingNotifiers: '/check_Accout/PMController/watch'
    // 注册申请中的代理商列表获取
    , regPendingFworkers: '/check_Accout/PMController/watch'
    // 注册申请中的代理商管理员列表获取
    , regPendingFAdmins: '/check_Accout/PMController/watch'
    // 审阅对账联系人注册
    , approveNotifier: '/check_Accout/PMController/verify_register'
    // 审阅代理商注册
    , approveFw: '/check_Accout/PMController/verify_register'
    // 审阅代理商管理员注册
    , approveFAdmin: '/check_Accout/PMController/verify_register'
    // 已注册的对账联系人
    , notifiers: '/check_Accout/PMController/watch'
    // 已注册代理商财务员
    , fworkers: '/check_Accout/PMController/watch'
    // 已注册的代理商方面的管理员列表
    , fadmins: '/check_Accout/PMController/watch'
    // 锁定、解锁联系人
    , ctrlNotifier: '/check_Accout/PMController/control_power'
    // 锁定、解锁代理商财务员
    , ctrlFworker: '/check_Accout/PMController/control_power'
    // 锁定、解锁代理商管理员
    , ctrlFAdmin: '/check_Accout/PMController/control_power'

    // 备份数据库操作
    , backupdb: '/check_Accout/PMController/backupdb'
    // 恢复数据库
    , restoredb: '/check_Accout/PMController/verify_backup'
    // 已备份数据库列表
    , dbbackups: '/check_Accout/PMController/choose_backup'
    // 日志获取
    , viewLog: '/check_Accout/PMController/watch'
    // 获取代理商列表
    , fetchAgents: '/check_Accout/PMController/get_agentcodeAname'

    // 积分模块
    // 超级管理员获取所有用户积分信息
    , scoreInAllAgents: '/check_Accout/ScoreController/all_scoreinfos'
    // 财务员获取所在代理商管理的用户的积分信息
    , scoreInAgent: '/check_Accout/ScoreController/agent_scoreinfos'
    // 超管、财务 -> 用户积分详情
    , scoreDetail: '/check_Accout/ScoreController/score_records'
    // 超级管理员 积分管理表
    , scoreMgmtAll: '/check_Accout/ScoreController/manage_exchange'
    // 财务员 积分管理表
    , scoreMgmtInAgent: '/check_Accout/ScoreController/agent_exchangeinfos'
    // 超级管理员↓
    // 导出积分报表
    , exportScoreTbl: '/check_Accout/ScoreController/download_scoreinfo'
    // 导出积分兑换报表
    , exportScoreExchgTbl: '/check_Accout/ScoreController/download_exchangeinfo'
    // 确认 积分兑换
    , approveScoreExchg: '/check_Accout/ScoreController/approval_exchange'
    // 下载礼品信息报表
    , exportGiftCat: '/check_Accout/ScoreController/download_giftinfo'
    // 物流详情
    , shipDetail: '/check_Accout/ScoreController/logistic_info'
    // 上传物流
    , uploadShipInfo: '/check_Accout/ScoreController/upload_logistic'
    // 上传礼品列表
    , uploadGiftCat: '/check_Accout/ScoreController/upload_gift'
};

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

var appConf = {
    //关账时间，月初5号
    checkClosingDate: 5
    , tmFmtLong: 'yyyy-MM-dd HH:mm:ss'
    , tmFmtYMD: 'yyyy-MM-dd'
    // , numberPerPage: 10
    , opLogResultTypes: ['成功', '失败']
    , opLogUserRoles: ['客户', '管理员', '代理商财务', '代理商管理']
    , userRegisterWays: ['个体户', '公司']
    , scoreStatusInTable: ['兑换中', '正常']
    // 兑换类型
    , exchangeTypes: ['红包', '礼品'],
    regEmailDomainRestrict: ['sanygroup.com']
    , mappings: {
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