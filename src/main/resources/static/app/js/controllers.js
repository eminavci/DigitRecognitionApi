'use strict';

/* Controllers */
var app = angular.module('hwdrApp.controllers', []);


app.controller('drController', ['$scope','$log', '$rootScope', '$location', 'entryService', 'helperService', function ($scope, $log, $rootScope, $location, entryService, helperService) {
    $scope.focusinControl = {};
    $scope.carryDraw = {};
    $scope.drawedImgStr = "";
    $scope.fccMeta = {};
     $scope.fccs = [];
    $scope.strentghBarStl = {wdth:"0px", bgclr: "#FF0000"};

    $scope.digits = 0;
    $scope.kforNN = 5;

    console.log('$locationeeeeeeeeeeeeeeee',$location.host());

    $scope.freemanHistData = {
        chart: {
            //caption: "Harry's SuperMart",
            subCaption: "Freeman Code Histogram",
            //numberPrefix: "$",
            theme: "fint"
        },
        data: [{label: "0",value: 0}, {label: "1",value: 0}, {label: "2",value: 0}, {label: "3",value: 0}, {label: "4",value: 0}, {label: "5",value: 0}, {label: "6",value: 0}, {label: "7",value: 0}]
    };

    $scope.clearBtn = function(){
        drawedCtx.clearRect(0, 0, drawedCanvas.width, drawedCanvas.height);
        $scope.fccMeta = {};
    };

    $scope.addToTrSet = function(){
          if(typeof $scope.fccMeta.reducedDrawedImg === "undefined"){
               helperService.initMSg(true, 2, "Draw Something!", 2500);
           } else {
               entryService.addToTrainingSet($scope.fccMeta, $scope.digits).success(function(response){

                }).error(function(response){
                    helperService.initMSg(true, response.msgType, response.errMsg, 7000);
               });
           }
    };

    $scope.classifyBtn = function(){
            if($scope.kforNN == undefined)
                $scope.kforNN = 5
           if(typeof $scope.fccMeta.reducedDrawedImg === "undefined"){
               helperService.initMSg(true, 2, "Draw Something!", 2500);
           } else {
               entryService.classify($scope.fccMeta.fccs, $scope.kforNN).success(function(response){
                   $scope.fccs = response.nearestFccs;
                   $scope.classificationTime = response.exec_time
                   $rootScope.$broadcast('drawDigit',{digit:response.predictedClass});

                   var barr = document.getElementById("main-strenth-bar");
                   var gWid = barr.offsetWidth;

                   var newWidth = Math.floor(gWid*response.strength/100);
                   $scope.strentghBarStl.wdth =  + newWidth + "px";

                   var colorss = ['#FE5959','#FD7171','#FD8A8A','#FEA4A4','#FDBBBB'];
                   var clrIdex = Math.floor(gWid/newWidth);

                   $scope.strentghBarStl.bgclr = colorss[clrIdex];

                   //barr.style.backgroundColor = 'blue';

               }).error(function(response){
                   console.log("ERROR : " + JSON.stringify(response));
                   helperService.initMSg(true, response.msgType, response.errMsg, 7000);
               });
           }
    };

    $scope.deleteFccEntry = function(idsi){
        entryService.deleteFccData(idsi).success(function(resonse){
            for (var i = 0; i < $scope.fccs.length; i++) {
                var fcc = $scope.fccs[i];
                if(fcc.id === idsi){
                    $scope.fccs.splice($scope.fccs.indexOf(fcc), 1);
                    break;
                }
            }
            helperService.initMSg(true, 3, "Deletion succed", 1500);
        }).error(function(response){
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };



}]);

app.controller('trController', ['$scope', '$route', 'entryService', 'helperService', 'cfpLoadingBar', function ($scope, $route, entryService, helperService, cfpLoadingBar) {

//    cfpLoadingBar.start(); // Start loading.
//    setTimeout( function() {
//        cfpLoadingBar.complete(); // End loading.
//    }, 4000);
    $scope.kTubeRun = false;
    $scope.showKhist=false;
    $scope.samplesHistData = {
        chart: {
            //caption: "",
            subCaption: "Samples Histogram",
            xAxisName: "Classes",
            yAxisName: "Frequencies",
            //numberPrefix: "$",
            theme: "fint"
        },
        data: [{label: "0",value: 0}, {label: "1",value: 0}, {label: "2",value: 0}, {label: "3",value: 0}, {label: "4",value: 0}, {label: "5",value: 0}, {label: "6",value: 0}, {label: "7",value: 0}, {label: "8",value: 0}, {label: "9",value: 0}]
    };

    $scope.khistorydata = {
        chart: {
            //caption: "Harry's SuperMart",
            subCaption: "K-Fold Cross Validation for kNN",
            //numberPrefix: "$",
            theme: "fint"
        },
        data: []
    };


  $scope.fccs = [];
  $scope.deleteds = [];
  entryService.getAllFccData().success(function(response){
      $scope.fccs = response.list;
  }).error(function(response){
      helperService.initMSg(true, response.msgType, response.errMsg, 7000);
  })

    $scope.deleteFccEntry = function(idsi){
        entryService.deleteFccData(idsi).success(function(resonse){
            for (var i = 0; i < $scope.fccs.length; i++) {
                var fcc = $scope.fccs[i];
                if(fcc.id === idsi){
                    $scope.fccs.splice($scope.fccs.indexOf(fcc), 1);
                    break;
                }
            }
            helperService.initMSg(true, 3, "Deletion succed", 1500);
        }).error(function(response){
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };

    $scope.condense = function(){
        entryService.condense().success(function(response){
            $scope.deleteds = response.list;
            var mssg = $scope.deleteds.length + " samples were removed from training set";
            helperService.initMSg(true, 3, mssg, 7000);
        }).error(function(response){
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };

    $scope.refreshpage = function(){
        $route.reload();
    };

    $scope.isInDeleteds = function(id){
         for (var i = 0; i < $scope.deleteds.length; i++) {
                var fcc = $scope.deleteds[i];
                if(fcc.id === id){
                    return true;
                }
            }
        return false;
    };


    $scope.kHistAttrs = {
        "caption": "K-Fold Cross Validation for kNN",
        "numberprefix": "",
        "plotgradientcolor": "",
        "bgcolor": "FFFFFF",
        "showalternatehgridcolor": "0",
        "divlinecolor": "7E7D7D",
        "xAxisName": "K for NN",
        "yAxisName": "Avg. Error",
        "showvalues": "0",
        "showcanvasborder": "0",
        "canvasborderalpha": "0",
        "canvasbordercolor": "8B0E0E",
        "canvasborderthickness": "1",
        "yaxismaxvalue": "10",
        "captionpadding": "4",
        "linethickness": "3",
        "xAxisLineColor": "#000000",
        "yaxisvaluespadding": "15",
        "legendshadow": "0",
        "legendborderalpha": "1",
        "palettecolors": "#008ee4, #f8bd19,#33bdda,#e44a00,#6baa01,#583e78",
        "showborder": "0",
        "divLineDashed": "1",
        "divLineDashLen": "1"
    };



    entryService.getNumberOfSamples().success(function(response){
        $scope.samplesHistData.data = response.list
    }).error(function(response){
        helperService.initMSg(true, response.msgType, response.errMsg, 7000);
    });


    $scope.kHistCats = [{"category":[]}];
    $scope.kHistData = [{"data":[],"seriesname":"Cros-validation error function"}];
    $scope.learnBestK = function(recompute){
        $scope.kTubeRun = true;
        entryService.tuneK(recompute).success(function(response){
            $scope.kTubeRun = false;
            var maxErr = 0;
            var category = {};
            category.category = [];
            var ds = {};
            ds.data = [];
            var ds2 = {};
            ds2.data = [];
            response.khistory.sort(compare);
            for(var ccat in response.khistory){
                var dd = {};
                dd.label = response.khistory[ccat].label;
                category.category.push(dd);
                if(response.K == dd.label){
                    var dd = {};
                    dd.vline = "true";
                    dd.lineposition = "0";
                    dd.color = "#DE1717";
                    dd.labelHAlign = "right";
                    dd.labelPosition = "0";
                    dd.label = "Optimum K=" + response.K;
                    category.category.push(dd);
                }


                var dsd = {};
                dsd.value = response.khistory[ccat].value+'';
                ds.data.push(dsd);

                var dsd2 = {};
                dsd2.value = response.khistory[ccat].value2+'';
                ds2.data.push(dsd2);

                if(response.khistory[ccat].value > maxErr)
                    maxErr = response.khistory[ccat].value;
                if(response.khistory[ccat].value2 > maxErr)
                    maxErr = response.khistory[ccat].value;
            }
            ds.seriesname="Error by counting Misclassified"
            ds2.seriesname="Error by how precise in kNearest"
            $scope.kHistAttrs.yaxismaxvalue = (maxErr);
            $scope.kHistCats = JSON.parse("[" + JSON.stringify(category) + "]");
            $scope.kHistData = JSON.parse("[" + JSON.stringify(ds) + ","+ JSON.stringify(ds2) + "]")

//            $scope.khistorydata.data = response.khistory;
              $scope.kHisRes = response;
        }).error(function(response){
            $scope.kTubeRun = false;
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };
    $scope.onChangeTestAcc = function(){

    }
    $scope.resAcc = [];
    $scope.testAccuracy = function(recompute){
        entryService.testAccuracy(recompute).success(function(response){
            $scope.resAcc = response.accuracyresult
            $scope.resAcc[0].matrix = JSON.parse(response.accuracyresult[0].matrix);
            $scope.resAcc[1].matrix = JSON.parse(response.accuracyresult[1].matrix);
            $scope.testType = $scope.resAcc[0];

        }).error(function(response){
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };

    function compare(a,b) {
        if (parseInt(a.label) < parseInt(b.label))
            return -1;
        if (parseInt(a.label) > parseInt(b.label))
            return 1;
        return 0;
    }

    $scope.learnBestK(false);
}]);

app.controller('ptrnController', ['$scope','$log', '$rootScope', 'entryService', 'helperService', function ($scope, $log, $rootScope, entryService, helperService) {

    $scope.patterns = [];
    $scope.classes = 0;
    $scope.freqMinRunTxt = "";

    $scope.getFreqPatterns = function(lblClass){
        entryService.getFrequentPatterns(lblClass).success(function(response){
            $scope.patterns = response.list;
        }).error(function(response){
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };

    $scope.computeFreqPatterns = function(){
        $scope.freqMinRunTxt = "Running..."
        entryService.computeFrequentPatterns().success(function(response){
            $scope.freqMinRunTxt = "Time : " + response.exec_time;
            $scope.classes = 0;
            $scope.getFreqPatterns(0);
            helperService.initMSg(true, 3, "Success. Execution Time : " + response.exec_time, 7000);
        }).error(function(response){
            $scope.freqMinRunTxt = "";
            helperService.initMSg(true, response.msgType, response.errMsg, 7000);
        });
    };

    $scope.getFreqPatterns(0);

}]);
app.controller('msgController', ['$scope', '$filter', '$log', '$timeout', 'helperService', function($scope, $filter, $log, $timeout, helperService) {

    $scope.msgData = {};

    var msgShowTimer = undefined;
  $scope.$watch(function () { return helperService.getisShowMsg(); }, function (newValue, oldValue) {

      if(newValue){
          $scope.msgData = helperService.getMSgData();
          msgShowTimer = $timeout(function(){
            helperService.setisShowMsgFalsaBack();
        }, $scope.msgData.showTime); // After 7 second... message box will close by itself;
      } else {
           $timeout.cancel(msgShowTimer);
      }
  });


}]);

/*app.controller('DummyCtrl', ['$scope', 'DummyFactory', function ($scope, DummyFactory) {

}]);*/
