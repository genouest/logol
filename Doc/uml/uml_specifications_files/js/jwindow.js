/**
 * JS Window framework
 * Library require: animate.js, shadow.js, dragdrop.js
 * @author Siri Chongasamethaworn (goonohc@gmail.com)
 * @version 1.0 November 22, 2006
 * @since November 22, 2006
 */
Library.load({'js/animate.js':['Graphics'],'js/shadow.js':['Shadow'],'js/dragdrop.js':['DragDrop']});var JWindow=function(aa){this.contentDiv=this.createDivs(aa);this.iframe;this.renderFunction;this.html='';this.height;this.width;this.position;this.dragingObject;this.MSIE=navigator.userAgent.indexOf('MSIE')>=0;window.jwindowObj=this;};JWindow.prototype={setRenderFunction:function(ba){this.renderFunction=ba;},setHtml:function(ca){this.html=ca;},setSize:function(da,ea){this.width=da;this.height=ea;},setPosition:function(x,y){this.position=new Point(x,y)},setContentClass:function(fa){if(this.contentDiv)this.contentDiv.className=fa;},setShadow:function(ga){this.contentDiv.shadow=ga;},setDragable:function(ha){this.contentDiv.dragdrop=ha;},setBackground:function(ia){if(ia){var ja=document.createElement('div');ja.id='window.backgroundDiv';ja.className='windowBackground';ja.style.left='0px';ja.style.top='0px';ja.style.zIndex=1;ja.style.background=ia==true?'#AAAAAA':ia;ja.style.position='absolute';this.contentDiv.backgroundDiv=ja;Graphics.setOpacity(this.contentDiv.backgroundDiv,40);if(this.MSIE){this.iframe=document.createElement('iframe');this.iframe.src='about:blank';this.iframe.frameborder=0;this.iframe.style.zIndex=900;this.iframe.style.position='absolute';}} else{this.contentDiv.backgroundDiv=null;}},elementWindow:function(){return this.contentDiv;},appendChild:function(ka){this.contentDiv.appendChild(ka);},show:function(){if(this.contentDiv&&!this.contentDiv.showing){document.body.appendChild(this.contentDiv);this.contentDiv.showing=true;if(this.contentDiv.shadow!=false)Shadow.castShadow(this.contentDiv);if(this.contentDiv.dragdrop!=false){DragDrop.makeDragable(this.contentDiv);this.contentDiv.style.cursor='move';} if(this.contentDiv.backgroundDiv)document.body.appendChild(this.contentDiv.backgroundDiv);this.insertContent();this.pack();}},hide:function(){document.documentElement.style.overflow='';if(this.contentDiv&&this.contentDiv.showing){document.body.removeChild(this.contentDiv);this.contentDiv.showing=false;Shadow.removeShadow(this.contentDiv);DragDrop.removeDragable(this.contentDiv);if(this.contentDiv.backgroundDiv)document.body.removeChild(this.contentDiv.backgroundDiv);}},createDivs:function(la){var ma=document.createElement('div');ma.id=la;ma.className='windowContent';ma.style.zIndex=1000;ma.style.position='absolute';return ma;},pack:function(){if(!this.contentDiv)return;var na=Math.max(document.body.scrollTop,document.documentElement.scrollTop);document.documentElement.style.overflow='hidden';var oa=Math.max(document.body.scrollLeft,document.documentElement.scrollLeft);var pa=Math.max(document.body.scrollTop,document.documentElement.scrollTop);setTimeout('window.scrollTo('+oa+','+pa+');',10);var qa=Graphics.findBodySize();if(this.width){this.contentDiv.style.width=this.width+'px';} if(this.height){this.contentDiv.style.height=this.height+'px'} if(this.position){if(this.position.x)this.contentDiv.style.left=this.position.x+'px';if(this.position.y)this.contentDiv.style.top=this.position.y+'px';} else{var ra=this.contentDiv.offsetWidth;var sa=this.contentDiv.offsetHeight;var ta=Math.ceil((qa.width-ra)>>1);var ua=Math.ceil((qa.height-sa)>>1)+na;this.contentDiv.style.left=ta+'px';this.contentDiv.style.top=ua+'px';} if(this.contentDiv.backgroundDiv){this.contentDiv.backgroundDiv.style.height=qa.height+'px';this.contentDiv.backgroundDiv.style.width=qa.width+'px';if(this.MSIE){if(this.contentDiv.style.width){this.iframe.style.width=this.contentDiv.style.width;} if(this.contentDiv.style.height){this.iframe.style.height=this.contentDiv.style.height;} this.iframe.style.left=this.contentDiv.style.left;this.iframe.style.top=this.contentDiv.style.top;}} if(this.contentDiv.shadow)this.contentDiv.shadow.repaint();},insertContent:function(){if(typeof(this.renderFunction)=='function'){setTimeout('window.jwindowObj.renderFunction()',15);} else if(this.html!=''){this.contentDiv.innerHTML=this.html;}}};