// 将2016-08格式的年月转为“2016年08月”这种对用户更友好的显示
app.filter('friendlyMonth', function () {
    return function (m) {
        var dash = m.indexOf('-');
        return m.substr(0, dash) + '年' + parseInt(m.substr(dash + 1)) + '月';
    }
});

// 映射状态代码为中文
app.filter('ctrlStatusFilter', function () {
    return function (flag) {
        switch (flag) {
            case 0:
                return '正常';
            case -3:
                return '锁定';
            default:
                console.warn('未匹配的管控状态：' + flag);
                return '';
        }
    }
});

app.filter('notifStatusPrinter', function () {
    return function translateNotifyStatus(statusCode) {
        switch (statusCode) {
            case 'Y':
                return '已通过';
            case 'N':
                return '被否决';
            case 'W':
                return '待定中';
            case 'M':
                return '已通过';
            case 'V':
                return '无';
            default:
                return '';
        }
    }
});

app.filter('userRolePrinter', function () {
    return function (rolecode) {
        switch (rolecode) {
            case "bu":
                return '代理商财务';
            case 'bm':
                return '管理员';
            default:
                return '其他';
        }
    };
});

app.filter('dateFlt', ['$filter', function ($filter) {
    return function (date, format, timezone) {
        format = format || appConf.tmFmtLong;
        return $filter('date')(date, format, timezone)
    }
}]);


app.filter('rmdsFilter', ['$filter', function ($filter) {
    var filterFilter = $filter('filter');
    var standardComparator = function standardComparator(obj, text) {
        // console.debug('stdcmp obj, txt', obj, text);
        text = ('' + text).toLowerCase();
        return ('' + obj).toLowerCase().indexOf(text) > -1;
    };

    return function (array, expression) {
        // console.log('arr, expr', array, expression);
        function customComparator(actual, expected) {
            // console.debug('act, expect', actual, expected);
            // console.debug('type act', typeof actual);

            var isBeforeActivated = expected.before;
            var isAfterActivated = expected.after;
            var isLower = expected.lower;
            var isHigher = expected.higher;
            var higherLimit;
            var lowerLimit;
            var itemDate;
            var queryDate;


            if (ng.isObject(expected)) {

                //date range
                if (expected.before || expected.after) {
                    try {
                        if (isBeforeActivated) {
                            higherLimit = expected.before;

                            itemDate = new Date(actual);
                            queryDate = new Date(higherLimit);

                            if (itemDate > queryDate) {
                                return false;
                            }
                        }

                        if (isAfterActivated) {
                            lowerLimit = expected.after;


                            itemDate = new Date(actual);
                            queryDate = new Date(lowerLimit);

                            if (itemDate < queryDate) {
                                return false;
                            }
                        }

                        return true;
                    } catch (e) {
                        return false;
                    }

                } else if (isLower || isHigher) {
                    //number range
                    if (isLower) {
                        higherLimit = expected.lower;

                        if (actual > higherLimit) {
                            return false;
                        }
                    }

                    if (isHigher) {
                        lowerLimit = expected.higher;
                        if (actual < lowerLimit) {
                            return false;
                        }
                    }

                    return true;
                }
                //etc

                return true;

            }
            return standardComparator(actual, expected);
        }

        var output = filterFilter(array, expression, customComparator);
        return output;
    };
}]);