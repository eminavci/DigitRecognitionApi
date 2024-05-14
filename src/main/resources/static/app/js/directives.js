'use strict';
var directives = angular.module('hwdrApp.directives', []);

directives.directive('onlyDigits', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
            function inputValue(val) {
                if (val) {
                    var digits = val.replace(/[^0-9]/g, '');

                    if (digits !== val) {
                        ctrl.$setViewValue(digits);
                        ctrl.$render();
                    }
                    return parseInt(digits,10);
                }
                return undefined;
            }
            ctrl.$parsers.push(inputValue);
        }
    };
});

directives.directive('drawedsmall', function(){
    return{
        restrict: 'E',
        scope:{deleteCtrlFn: '&deleteFccEntry'},
        link: function(scope,e,a){
            scope.fcc =  scope.$parent.fccs[a.idm];
            scope.deleteds = scope.$parent.isim;
            scope.deleteFccData = function(idsi){
                scope.deleteCtrlFn(idsi);
            };
        },
        replace: true,
        templateUrl:"/app/view/drawedsmall.html"
    };
});

directives.directive('popupadd',['$timeout', function($timeout){
    return{
        restrict: 'E',
        scope: {control2: '=', addingTraining: '&callbackAddTrset'},
        link: function(scope,e,a){
            scope.internalControl2 = scope.control2 || {};

            scope.yesClicked = function(){
                scope.addingTraining();
                 $timeout(function(){
                    scope.showPopup = false;
                 }, 250);
            };

            scope.internalControl2.drawDrawed = function(dgt, drawedUrl){
                if(typeof drawedUrl !== "undefined"){
                    scope.showPopup = true;
                    scope.digits = dgt;
                    scope.drawedImg = drawedUrl;
                }
            };

        },
        replace: true,
        templateUrl:"/app/view/popupadd.html"
    };
}]);

directives.directive("drawinger", ['entryService', 'helperService', function(entryService, helperService){
    var ms = {"curr" : 0, "prev" : 0};
    var mouseStatus = 0;
    var drawStatus = 0;
    var mouseDown = 0;
  return {
    restrict: "A",
    scope: {
      control: '='
    },
    link: function(scope, element){
        scope.internalControl = scope.control || {};
        var width = element[0].clientWidth;
        var height = element[0].clientHeight;
        var canvas = document.createElement('canvas');
        var canvas2 = document.createElement('canvas');
        var ctx = canvas.getContext('2d');
        var ctx2 = canvas2.getContext('2d');
        canvas.id = 'canvas';
        canvas.width = width;
        canvas.height = height;

        canvas2.id = 'canvas2';
        canvas2.width = width/2;
        canvas2.height = height/2;

        ctx.strokeStyle = "black";
        ctx.lineJoin = 'round';
        ctx.lineCap = 'round';
        ctx.lineWidth = 10;

        ctx2.strokeStyle = "black";
        ctx2.lineJoin = 'round';
        ctx2.lineCap = 'round';
        ctx2.lineWidth = 10;

        ctx.font="140px Verdana";
        var gradient=ctx.createLinearGradient(0,0,canvas.width,0);
        gradient.addColorStop("1.0","#3C53F3");
        gradient.addColorStop("1.0","blue");
        gradient.addColorStop("1.0","red");
        // Fill with gradient
        ctx.fillStyle=gradient;
        ctx2.fillStyle=gradient;

        element[0].appendChild(canvas);

      // variable that decides if something should be drawn on mousemove
      var drawing = false;
      // the last coordinates before the current move
      var lastX;
      var lastY;

      element.bind('mousedown', function(event){
          mouseDown = 1;
        if(ms.curr === 1 && ms.prev === 0){
            scope.internalControl.reset();
            ms.prev = 1;
        }
        lastX = typeof event.offsetX !== 'undefined' ? event.offsetX : event.layerX;
        lastY = typeof event.offsetY !== 'undefined' ? event.offsetY : event.layerY;
        ctx.beginPath();
          ctx2.beginPath();

        drawing = true;
      });

      element.bind('mousemove', function(event){
        if(drawing){
          var currentX = typeof event.offsetX !== 'undefined' ? event.offsetX : event.layerX;
          var currentY = typeof event.offsetY !== 'undefined' ? event.offsetY : event.layerY;
          draw(lastX, lastY, currentX, currentY);
          lastX = currentX;
          lastY = currentY;
        }
      });

      element.bind('mouseup', function(event){
          mouseDown = 0;
        // stop drawing
        drawing = false;
        ms.prev = ms.curr;
        ms.curr = 1;
      });

      element.bind('mouseover', function(event){
        ms = {"curr" : 1, "prev" : 0};
      });

      element.bind('mouseout', function(event){
          if(drawStatus === 1 && mouseDown == 0){
                var dataURL = canvas.toDataURL("image/png");
                var reduceDataImgUrl = canvas2.toDataURL("image/png");
                entryService.computeFreeman(dataURL).success(function(response){

                    var image = new Image();
                    image.onload = function() {
                        ctx.drawImage(image, 0, 0);
                    };
                    image.src = "data:image/png;base64,"+response.counteredBase64ImgStr;
                    scope.$parent.fccMeta = response;
                    //console.log("ORIG FCC LENGTH : " + response.fccs[0].fcc.length);
                    scope.$parent.fccMeta.fccs[0].fcc = "";
                    scope.$parent.fccMeta.reducedDrawedImg = reduceDataImgUrl.split(',')[1];
                    scope.$parent.fccMeta.drawedBase64ImgStr = dataURL;
                    entryService.computeFreeman(reduceDataImgUrl).success(function(resp){
                         scope.$parent.fccMeta.fccs[0].fcc = resp.fccs[0].fcc;
                        computeFcHistogram(resp.fccs[0].fcc)


                        //console.log("REDUCED FCC LENGTH : " + resp.fccs[0].fcc.length);
                    }).error(function(resp){
                        console.error("reducedDrawedImg error" + resp.status+ " " + resp.errMsg);
                    });
                    //scope.$parent.fccMeta.drawedImg = dataURL;
                }).error(function(response){
                    helperService.initMSg(true, response.msgType, response.errMsg, 7000);
                });
                  drawStatus = 0;
          }
          ms = {"curr" : 0, "prev" : 1};

      });

      // canvas reset
      scope.internalControl.reset = function(){
        ctx.save();
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.restore();

        ctx2.save();
        ctx2.setTransform(1, 0, 0, 1, 0, 0);
        ctx2.clearRect(0, 0, canvas.width, canvas.height);
        ctx2.restore();

        drawStatus = 0;
      }

       scope.drawDigit = function(data){
          scope.internalControl.reset();
          ctx.fillText(data.digit,60,150);
      }

       scope.$on('drawDigit',function(event, data){
             scope.drawDigit(data)
       });


      function computeFcHistogram(fcCode){
          var letters = new Object;

          length = fcCode.length;
          console.log("LENTHGGG : " + length);

          for(var m = 0;  m < length; m++) {
              var l = fcCode.charAt(m)
              letters[l] = (isNaN(letters[l]) ? 1 : letters[l] + 1);
          }

          //output count!
          for(var key in letters) {
              scope.$parent.freemanHistData.data[key].value = letters[key]
          }
      }

      function draw(lX, lY, cX, cY){
        drawStatus = 1;

        ctx.moveTo(lX,lY);
        ctx.lineTo(cX,cY);
        // draw it
        ctx.stroke();

        ctx2.moveTo(Math.floor(lX/2),Math.floor(lY/2));
        ctx2.lineTo(Math.floor(cX/2),Math.floor(cY/2));
        // draw it
        ctx2.stroke();
      }
    }
  };
}]);


directives.directive( "mwConfirmClick", [
  function( ) {
    return {
      priority: -1,
      restrict: 'A',
      scope: { confirmFunction: "&mwConfirmClick" },
      link: function( scope, element, attrs ){
        element.bind( 'click', function( e ){
          // message defaults to "Are you sure?"
          var message = attrs.mwConfirmClickMessage ? attrs.mwConfirmClickMessage : "Are you sure?";
          // confirm() requires jQuery
          if( confirm( message ) ) {
            scope.confirmFunction();
          }
        });
      }
    }
  }
]);













