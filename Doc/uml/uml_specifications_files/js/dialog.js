/**
 * JS & AJAX Dialog framework
 * Library require: jwindow.js
 * @author Siri Chongasamethaworn (goonohc@gmail.com)
 * @version 2.0 March 13, 2007
 * @since November 22, 2006
 */
Library.load({'js/jwindow.js':['JWindow']});var Dialog=function(){this.dialog=new JWindow('dialog');this.dialog.setContentClass('dialogContent');this.dialog.setBackground('#AAAAAA');this.dialog.setDragable(false);window.dialogObj=this;};Dialog.prototype={setRenderFunction:function(aa){this.dialog.setRenderFunction(aa)},setHtml:function(ba){this.dialog.setHtml(ba);},setSize:function(ca,da){this.dialog.setSize(ca,da);},setContentClass:function(ea){this.dialog.setContentClass(ea);},setBackgroundColor:function(fa){this.dialog.setBackground(fa);},setShadow:function(ga){this.dialog.setShadow(ga);},getWindow:function(){return this.dialog;},appendChild:function(ha){this.dialog.appendChild(ha);},show:function(){this.dialog.show();},hide:function(){this.dialog.hide();},pack:function(){this.dialog.pack();}};