'use strict';
var services = angular.module('hwdrApp.services', ['ngResource']);
var baseUrl = '/hwdr-api';


services.service('entryService',['$http', 'helperService', function($http, helperService){
    var self = this;

    self.computeFreeman = function(data){
        helperService.setisShowMsgFalsaBack();
        return $http.post(baseUrl + "/computeFreeman", data);
    };

    self.addToTrainingSet = function(dd, lblClass){
        helperService.setisShowMsgFalsaBack();
        var data = {};
        data.reducedBase64ImgStr = dd.reducedDrawedImg;
        data.drawedBase64ImgStr = dd.drawedBase64ImgStr
        data.lblClass = lblClass;
        return $http.post(baseUrl + "/addToTrainingSet", data);
    };

    self.getAllFccData = function(){
        helperService.setisShowMsgFalsaBack();
      return $http.get(baseUrl+"/getAllFccData");
    };

    self.deleteFccData = function(idsi){
        helperService.setisShowMsgFalsaBack();
        return $http.get(baseUrl + "/deleteFccData/" + idsi);
    };

    self.classify = function(fcc2, k){
        helperService.setisShowMsgFalsaBack();
        var data = {};
        data.fccs = [];
        for (var i = 0, len = fcc2.length; i < len; i++) {
            data.fccs.push(fcc2[i].fcc)
        }
        data.K = k
        return $http.post(baseUrl + "/classify", data);
    };

    self.condense = function(){
        helperService.setisShowMsgFalsaBack();
        return $http.get(baseUrl + "/condense");
    };

    self.getNumberOfSamples = function(){
        helperService.setisShowMsgFalsaBack();
        return $http.get(baseUrl + "/numberofsamples");
    };

    self.tuneK = function(recompute){
        helperService.setisShowMsgFalsaBack();
        var urll = baseUrl + "/learnk?recompute=" + recompute;
        if(recompute){
            return $http.get(urll);
        } else {
            return $http.get(urll, {ignoreLoadingBar: true});
        }
    };
    self.getFrequentPatterns = function(lblClass){
        helperService.setisShowMsgFalsaBack();
        var urll = baseUrl + "/getfrequentpatterns?lblClass=" + lblClass;
        return $http.get(urll);
    };

    self.computeFrequentPatterns = function(){
        helperService.setisShowMsgFalsaBack();
        return $http.get(baseUrl + "/computefrequentpatterns");
    };
    self.testAccuracy = function(recompute){
        helperService.setisShowMsgFalsaBack();
        return $http.get(baseUrl + "/testacc?recompute=" + recompute);
    };
}]);

services.factory('helperService', ['$http', '$log', function($http, $log){

    var msgData = {};

    msgData.isShowMsg = false;
    msgData.msgType = 0; // 0:Error, 1:Info, 2:Warn, 3:Success
    msgData.customMsg = "An Error Occured! Sorry!";
    msgData.showTime = 5000;

    return {
        initMSg : function(isShow, msgTip, cstMSg, showTime){
            if(!isShow){
                $log.error("helperService.initMSg can not be initilized with false value!");
                return;
            }
           msgData.isShowMsg = isShow;
           msgData.customMsg = cstMSg;
           msgData.msgType = msgTip;
           msgData.showTime = showTime;
        },
        getisShowMsg: function(){
            return msgData.isShowMsg;
        },
        setisShowMsgFalsaBack: function(){
          msgData.isShowMsg = false;
        },
        getMSgData: function(){
            return msgData;
        }
    };

}]);


/*services.factory('DummyFactory', function ($resource) {
    return $resource(baseUrl + '/ngdemo/web/dummy', {}, {
        query: { method: 'GET', params: {} }
    })
});

services.factory('UsersFactory', function ($resource) {
    return $resource(baseUrl + '/ngdemo/web/users', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});

services.factory('UserFactory', function ($resource) {
    return $resource(baseUrl + '/ngdemo/web/users/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});*/
