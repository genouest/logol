/**
 * Tooltip control
 * Library require: animate.js, shadow.js
 * @author Siri Chongasamethaworn (goonohc@gmail.com)
 * @version 1.0 November 16, 2006
 */
Library.load({"js/animate.js":["Graphics"]});var ToolTip=function(){window.toolTipObj=this;this.addEvent(document,"mousemove",this.mouseMove)};ToolTip.prototype={addEvent:function(C,A,B){if(typeof document.attachEvent=="undefined"){C.addEventListener(A,B,false)}else{C.attachEvent("on"+A,B)}},createTooltip:function(){var A=document.createElement("div");A.id="tooltipDiv";A.style.position="absolute";A.style.visibility="hidden";A.style.zIndex=90;return A},createHeader:function(A){var B=document.createElement("div");B.id="tooltipHeader";if(A){return B}B.style.fontWeight="bold";B.style.fontFamily="arial";B.style.border="#A5CFE9 solid 1px";B.style.borderBottom="none";B.style.padding="3px";B.style.fontSize="x-small";B.style.color="#4B7A98";B.style.background="#D5EBF9";B.style.textAlign="center";return B},createBody:function(A){var B=document.createElement("div");B.id="tooltipBody";if(A){return B}B.style.border="#A5CFE9 solid 1px";B.style.fontFamily="arial";B.style.fontSize="x-small";B.style.padding="3px";B.style.color="#1B4966";B.style.background="#FFFFFF";B.style.textAlign="left";return B},createBodyWithoutHeader:function(A){var B=document.createElement("div");B.id="tooltipBody";if(A){return B}B.style.border="#A5CFE9 solid 1px";B.style.fontFamily="arial";B.style.fontSize="x-small";B.style.padding="3px";B.style.color="#1B4966";B.style.background="#ECF2F9";B.style.textAlign="left";return B},getParam:function(G,F){var B=G.indexOf(F+"[");var A="";if(B>=0){var C=G.length;var E=1;var D=false;B+=F.length+1;while(B<C){var H=G.charAt(B++);if(D){A+=H;D=false}else{switch(H){case"[":E++;if(E!=1){A+=H}break;case"]":E--;if(E!=0){A+=H}break;case"\\":D=true;break;default:A+=H}if(E==0){break}}}}return E==0?A:null},scan:function(K){if(K.title){var I=K.title;if(K.title.indexOf("body[")>=0){I=this.getParam(K.title,"body")}var G=this.getParam(K.title,"header");var H=this.getParam(K.title,"cssbody");var M=this.getParam(K.title,"cssheader");var B=this.getParam(K.title,"offsetx");var A=this.getParam(K.title,"offsety");var E=this.getParam(K.title,"delay");var D=this.getParam(K.title,"fade");var C=this.getParam(K.title,"fadespeed");var J=this.getParam(K.title,"opacity");var N=this.createTooltip();if(G){var F=this.createHeader(M!=null);F.innerHTML=G;F.className=M;if(G.length>200){N.style.width="300px"}N.appendChild(F)}if(I){var L;if(G){L=this.createBody(H!=null)}else{L=this.createBodyWithoutHeader(H!=null)}L.innerHTML=I;L.className=H;if(I.length>200){N.style.width="300px"}N.appendChild(L)}N.offsetX=B?parseInt(B):10;N.offsetY=A?parseInt(A):10;N.delay=E?parseInt(E):0;N.fade=D?D:true;N.fadespeed=C?parseInt(C):400;N.opacity=J?parseInt(J):90;K.tooltip=N;this.addEvent(K,"mouseout",function(){window.toolTipObj.hide(window.toolTipObj.tooltip)});this.addEvent(N,"mouseout",function(){window.toolTipObj.hide(window.toolTipObj.tooltip)});K.title="";K.hasTooltip=true}else{K.hasTooltip=false}},mouseMove:function(D){var A=D?D:event;var C=A.target?A.target:A.srcElement;if(C.nodeType==3){C=C.parentNode}if(C.hasTooltip==null){if(window.toolTipObj){window.toolTipObj.scan(C)}}if(C.tooltip&&window.toolTipObj.tooltip!=C.tooltip){if(window.toolTipObj.tooltip!=null){window.toolTipObj.hide(window.toolTipObj.tooltip)}window.toolTipObj.tooltip=C.tooltip;document.body.appendChild(C.tooltip);var B=Graphics.mousePosition(A);C.tooltip.style.left=B.x+C.tooltip.offsetX+"px";C.tooltip.style.top=B.y+C.tooltip.offsetY+"px";C.tooltip.timeoutId=setTimeout("window.toolTipObj.show(window.toolTipObj.tooltip)",C.tooltip.delay)}},show:function(A){if(!A){return }if(A.fade){Graphics.setOpacity(A,0);A.style.visibility="visible";Graphics.changeOpacity(A,0,A.opacity,A.fadespeed)}else{Graphics.setOpacity(A,A.opacity);A.style.visibility="visible"}},hide:function(A){if(!A){return }A.style.visibility="hidden";clearTimeout(A.timeoutId);try{document.body.removeChild(A)}catch(B){}window.toolTipObj.tooltip=null}};var tooltip=new ToolTip();