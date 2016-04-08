/**
 * Contains functions for Web Publisher 2.0
 *
 * @author Siri Chongasamethaworn (siri_c@nomagicasia.com)
 * @version 1.1 March 17, 2008
 */





var nonamedNode = '< >';
var nonamedLink = '';
var resourcesLocation = contextPath?contextPath:'uml_specifications_files';
if (resourcesLocation!='' && !/\/$/.test(resourcesLocation))
   resourcesLocation = resourcesLocation + '/';
Library.load({'js/animate.js': ['Graphics', 'Content']});
Library.load({'js/cookies.js': ['Cookies']});
Content.imgShow = resourcesLocation + 'images/down_triangle.gif';
Content.imgHide = resourcesLocation + 'images/right_triangle.gif';
addEvent(window, 'load', repaint);
addEvent(window, 'load', initWeb);
addEvent(window, 'resize', function(){resize();});
var tree;
var viewbar;
var actionbar;
var currentPageId;

var backStack = new Stack();
var forwardStack = new Stack();
function resize()
{
   var splitpane = document.getElementById('splitpane');
   splitPane.repaint();
}
function repaint()
{
   var divs = document.getElementsByTagName('div');
   for (var i=0; i<divs.length; i++)
   {
      var className = divs[i].className + ' ';
      if (className.indexOf("thead") != -1)
      {
         var headerText = divs[i].innerHTML;
         var contentNode = nextSibling(divs[i]);
         if (divs[i].hasChildNodes() && contentNode.id)
         {
            var img = document.createElement('img');
            img.src = Content.imgShow;
            img.alt = '';
            img.style.margin = '.1em';
            img.contentId = contentNode.id;
            img.onclick = function()
            {
               Content.showHide(this, this.contentId);
            };
            divs[i].insertBefore(img, divs[i].childNodes[0]);
         }
      }
   }
   splitPane.repaint();
}

function initWeb()
{
   var nav = new Navigator();
   nav.menuLeftImg = 'url('+resourcesLocation+'images/navigator/containment_left.gif)';
   nav.menuLeftOverImg = 'url('+resourcesLocation+'images/navigator/containment_left_over.gif)';
   nav.menuRightImg = 'url('+resourcesLocation+'images/navigator/containment_right.gif)';
   nav.menuRightOverImg = 'url('+resourcesLocation+'images/navigator/containment_right_over.gif)';
   nav.repaint();
   var splitpane = document.getElementById('splitpane');
   if (splitpane)
   {
      var containmentButton = nav.getMenuLeftIcon();
      containmentButton.id = 'containmentButton';
      var cell = splitpane.rows[0].insertCell(0);
      cell.id = 'menupane';
      cell.style.verticalAlign = 'top';
      cell.appendChild(containmentButton);
      var containmentButtonWidth = ((containmentButton.offsetWidth / splitpane.offsetWidth) * 100);
      cell.style.width = containmentButtonWidth + '%';
   }
   var browser = document.getElementById('browser');
   if (browser)
   {
      var bar = document.createElement('div');
      bar.className = 'browserbar';
      if (browser.hasChildNodes())
         browser.insertBefore(bar, browser.childNodes[0]);
      else
         browser.appendChild(bar);
      bar.appendChild(nav.getUnDockIcon());
      bar.appendChild(nav.getMinimizeIcon());
      bar.appendChild(document.createTextNode('Containment'));
   }
   var titlebar = document.getElementById('titlebar');
   var content = document.getElementById('splitpane-second');
   if (document.documentElement.clientHeight)
      content.style.height = document.documentElement.clientHeight - titlebar.offsetHeight + 'px';
   else
      content.style.height = window.innerHeight - titlebar.offsetHeight + 'px';
   if (Cookies.getCookie('Navigator.pin')=='false') nav.togglePin(false);
}

function isHiddenNode(node)
{
   if (node.nodeType==1)
   {
      // do not display hidden element
      var isHidden = node.getAttribute('isHidden');
      if (isHidden == 'true') return true;
      return false;
   }
   return true;
}

function buildTree(li)
{
   var model = li.data;
   if (model == null) return;
   var childNodes = model.childNodes;
   for (var c=0; c<childNodes.length; c++)
   {
      if (childNodes[c].tagName == 'ownedElement')
      {
         var members = childNodes[c].childNodes;
         for (var m = 0; m<members.length; m++)
         {
            if (isHiddenNode(members[m])) continue;
            var emptyUL = document.createElement('ul');
            emptyUL.onExpand = function() {
               var node = this.parentNode;
               if (this.hasChildNodes()) { return; }
               var childNodes = node.data.childNodes;
               for (var c=0; c<childNodes.length; c++)
               {
                  if (childNodes[c].tagName == 'ownedElement')
                  {
                     var groupMap = new Array();
                     var members = childNodes[c].childNodes;
                     for (var p = 0; p<members.length; p++)
                     {
                        if (members[p].nodeType==1)
                        {
                           if (isHiddenNode(members[p]) || members[p].tagName == 'diagram') continue;
                           // display element, icon
                           var icon = members[p].getAttribute('icon');
                           var childNode = null;
                           // group relationship
                           if (members[p].getAttribute('isRelationship')=='true')
                           {
                              var relationUL;
                              if (this.firstChild && this.firstChild.elementName == 'Relations')
                                 relationUL = this.firstChild.lastChild;
                              else
                              {
                                 var relationLI = addNode(this, 'Relations', 'javascript:void(0);', 'uml_specifications_files/icon_709918512.png');
                                 relationLI.setAttribute('refid', 'relations');
                                 relationUL = document.createElement('ul');
                                 relationUL.onExpand = function() {
                                 };
                                 relationUL.onCollapse = function() {
                                 };
                                 relationLI.appendChild(relationUL);
                                 if (this.firstChild)
                                    this.insertBefore(relationLI, this.firstChild);
                                 else
                                    this.appendChild(relationLI);
                                 tree.renderNode(relationLI);
                              }
                              var name = members[p].getAttribute('humanType');
                              if (members[p].getAttribute('name'))
                                 name += ':' + members[p].getAttribute('name');
                              childNode = addNode(relationUL, name, "javascript: showSpec('"+members[p].getAttribute('refid')+"');", icon);
                              childNode.data = members[p];
                              childNode.setAttribute('refid', members[p].getAttribute('refid'));
                           }
                           else
                           {
                              var groupBy = members[p].getAttribute('groupBy');
                              if (groupBy)
                              {
                                 if (!groupMap[groupBy]) groupMap[groupBy] = new Array();
                                 groupMap[groupBy][groupMap[groupBy].length] = members[p];
                              }
                              else
                              {
                                 var name;
                                 if (members[p].getAttribute('name'))
                                    name = members[p].getAttribute('name');
                                 else
                                    name = nonamedNode||nonamedNode==''?nonamedNode:members[p].getAttribute('humanType');
                                 childNode = addNode(this, name, "javascript: showSpec('"+members[p].getAttribute('refid')+"');", icon);
                                 childNode.data = members[p];
                                 childNode.setAttribute('refid', members[p].getAttribute('refid'));
                                 if (groupMap)
                                 {
                                    var childGroup = groupMap[childNode.getAttribute('refid')];
                                    if (childGroup)
                                    {
                                       var tmpOwnedElement;
                                       var tmpChildNodes = childNode.data.childNodes;
                                       for (var tmp=0; tmp<tmpChildNodes.length; tmp++)
                                       {
                                          if (tmpChildNodes[tmp].tagName=='ownedElement')
                                          {
                                             tmpOwnedElement = tmpChildNodes[tmp];
                                             break;
                                          }
                                       }
                                       if (typeof(tmpOwnedElement)=='undefined')
                                       {
                                          tmpOwnedElement = createElement('ownedElement');
                                          childNode.data.appendChild(tmpOwnedElement);
                                       }
                                       if (tmpOwnedElement)
                                       {
                                          for (var g=0; g<childGroup.length; g++)
                                          {
                                             childGroup[g].removeAttribute('groupBy');
                                             tmpOwnedElement.appendChild(childGroup[g]);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                           if (childNode != null) buildTree(childNode);
                        }
                     }
                  }
                  else if (childNodes[c].tagName == 'ownedDiagram')
                  {
                     var members = childNodes[c].childNodes;
                     for (var m = 0; m<members.length; m++)
                     {
                        if (members[m].nodeType==1)
                        {
                           var icon = members[m].getAttribute('icon');
                           var name = members[m].getAttribute('name');
                           var childNode = addNode(this, name, "javascript: showSpec('"+members[m].getAttribute('refid')+"');", icon);
                           childNode.data = members[m];
                           childNode.setAttribute('refid', members[m].getAttribute('refid'));
                           // comment below and uncomment buildTree(childNode) to see diagram inner node
                           tree.renderNode(childNode);
                           // buildTree(childNode);
                        }
                     }
                  }
               }
            };
            emptyUL.onCollapse = function() {
               splitPane.repaint();
            };
            li.appendChild(emptyUL);
            break;
         }
      }
   }
   tree.renderNode(li);
}

/**
 * Search and return tree node from refid
 * @param refid refid
 * @return LI tree node
 */
function findNode(refid)
{
   var searchResults = new Array(0);
   var dataModel = tree.root.firstChild.data;
   var regx = new RegExp(refid, 'i');
   match(dataModel, 'refid', regx, searchResults);
   if (searchResults.length == 1)
   {
      var parentNode = searchResults[0];
      var nodePath = new Array();
      while (parentNode && parentNode.tagName != 'magicdraw')
      {
         if (parentNode.nodeType==1)
         {
            var refid = parentNode.getAttribute('refid');
            if (refid)
               nodePath[nodePath.length] = refid;
            parentNode = parentNode.parentNode;
         }
      }
      var rootTree = document.getElementById(tree.treeId);
      expandPath(nodePath);
      searchResults = new Array(0);
      match(rootTree, 'refid', regx, searchResults);
      return searchResults[0];
   }
   return null;
}

/**
 * Select node on containment tree
 * @param node LI or A element of tree node
 */
function selectNode(node)
{
   if (node)
   {
      if (node.tagName == 'LI')
      {
         var childNodes = node.childNodes;
         for (var i=0; i<childNodes.length; i++)
         {
            if (childNodes[i].name == 'anchorNode')
            {
               var root = document.getElementById(tree.treeId);
               var nodes = root.getElementsByTagName('li');
               for (var n=0; n<nodes.length; n++)
               {
                  var anchorNodes = nodes[n].childNodes;
                  for (var a=0; a<anchorNodes.length; a++)
                  {
                     if (anchorNodes[a].name == 'anchorNode')
                        anchorNodes[a].style.backgroundColor = '';
                  }
               }
               childNodes[i].style.backgroundColor = '#99CCFF';
            }
         }
      }
      else if (node.tagName == 'A')
      {
         if (node.name == 'anchorNode')
         {
            var root = document.getElementById(tree.treeId);
            var nodes = root.getElementsByTagName('li');
            for (var n=0; n<nodes.length; n++)
            {
               var anchorNodes = nodes[n].childNodes;
               for (var a=0; a<anchorNodes.length; a++)
               {
                  if (anchorNodes[a].name == 'anchorNode')
                     anchorNodes[a].style.backgroundColor = '';
               }
            }
            node.style.backgroundColor = '#99CCFF';
         }
      }
   }
}

function addNode(ul, nodeName, href, icon)
{
   var node = document.createElement('li');
   node.elementName = nodeName;
   var anchor = document.createElement('a');
   anchor.appendChild(document.createTextNode(nodeName));
   anchor.name = 'anchorNode';
   anchor.href = href;
   anchor.style.verticalAlign = 'middle';
   anchor.style.marginLeft = '4px';
   anchor.style.marginRight = '4px';
   anchor.onclick = function(){
      selectNode(this);
   };
   node.appendChild(anchor);
   if (icon)
   {
      var imgAnchor = document.createElement('a');
      imgAnchor.href = href;
      imgAnchor.style.verticalAlign = 'middle';
      imgAnchor.onclick = anchor.onclick;
      var img = document.createElement('img');
      img.src = icon;
      img.alt = '';
      img.border = '0';
      img.height = '16';
      img.width = '16';
      img.style.verticalAlign = 'middle';
      imgAnchor.appendChild(img);
      node.insertBefore(imgAnchor, anchor);
   }
   ul.appendChild(node);
   return node;
}
/**
 * Shortcut to create HTML element with link
 * @param parentNode link container
 * @param linkToElement DOM element
 */
function createLink(parentNode, linkToElement)
{
   var refid = linkToElement.getAttribute('refid');
   var name = linkToElement.getAttribute('name');
   var icon = linkToElement.getAttribute('icon');
   if (icon)
   {
      var fieldAnchor = document.createElement('a');
      fieldAnchor.href = "javascript: showSpec('"+refid+"');";
      var fieldImage = document.createElement('img');
      fieldImage.alt = '';
      fieldImage.border = '0';
      fieldImage.height = '16';
      fieldImage.width = '16';
      fieldImage.src = icon;
      fieldAnchor.appendChild(fieldImage);
      parentNode.appendChild(fieldAnchor);
   }
   if (!name) name = nonamedLink||nonamedLink==''?nonamedLink:linkToElement.tagName;
   if (refid && name != '')
   {
      var fieldAnchor = document.createElement('a');
      fieldAnchor.href = "javascript: showSpec('"+refid+"');";
      fieldAnchor.style.marginLeft = '4px';
      fieldAnchor.style.marginRight = '4px';
      fieldAnchor.appendChild(document.createTextNode(name));
      parentNode.appendChild(fieldAnchor);
   }
   else
   {
      parentNode.appendChild(document.createTextNode(name));
   }
   var additionalText = linkToElement.getAttribute('text');
   if (additionalText)
   {
      var cite = document.createElement('cite');
      cite.style.marginLeft = '4px';
      cite.style.marginRight = '4px';
      renderValueText(cite, additionalText);
      parentNode.appendChild(cite);
   }
}

function selectView(view)
{
   viewbar.currentView = view;
   var viewtab = document.getElementById('viewtab');;
   var sib = firstChild(viewtab);
   while (sib != null)
   {
      sib.className = '';
      if (sib.tabName == view)
      {
         sib.className = 'active';
         sib.style.display = 'block';
      }
      sib = nextSibling(sib);
   }
   var modeItem = document.getElementById('modeItem');
   if (view == 'specification')
      modeItem.setEnabled(true);
   else
      modeItem.setEnabled(false);
}

function createViewBar(model)
{
   if (viewbar) return viewbar;
   // lazy initialize
   viewbar = document.createElement('div');
   viewbar.id = 'viewbar';
   viewbar.currentType = model.tagName=='diagram'?'diagram':'element';
   viewbar.currentView = 'specification';
   // tab bar
   var tabul = document.createElement('ul');
   tabul.id = 'viewtab';
   tabul.className = 'tab';
   var diali = document.createElement('li');
   diali.id = 'diagramtab';
   diali.tabName = 'diagram';
   diali.appendChild(document.createTextNode('Diagram'));
   diali.onclick = function() {
      selectView(this.tabName);
      var content = document.getElementById('content');
      renderDiagram(content.model);
      repaint();
   };
   tabul.appendChild(diali);
   var speli = document.createElement('li');
   speli.id = 'specificationtab';
   speli.tabName = 'specification';
   speli.appendChild(document.createTextNode('Specification'));
   speli.onclick = function() {
      selectView(this.tabName);
      var content = document.getElementById('content');
      renderElement(content.model);
      repaint();
   };
   tabul.appendChild(speli);
   viewbar.appendChild(tabul);

   // view mode
   var content = document.getElementById('content');
   if (typeof(content.mode)=='undefined')
      content.mode = 'standard';
   var modeItem = document.createElement('li');
   modeItem.id = 'modeItem';
   modeItem.style.cssFloat = 'right';
   modeItem.style.styleFloat = 'right';
   modeItem.style.margin = '0';
   modeItem.style.padding = '2px .5em 0 .5em';
   modeItem.style.cursor = 'default';
   modeItem.setEnabled = function(enabled) {
      var childNodes = this.childNodes;
      for (var c=0; c<childNodes.length; c++)
         childNodes[c].disabled = !enabled;
   };
   // mode label
   var modeLabel = document.createElement('div');
   modeLabel.title = 'Display properties by selected filter';
   modeLabel.className = 'item';
   modeLabel.style.cssFloat = 'left';
   modeLabel.style.styleFloat = 'left';
   modeLabel.appendChild(document.createTextNode('Mode : '));
   modeItem.appendChild(modeLabel);
   // move options
   var modeSelect = document.createElement('select');
   modeSelect.id = 'modeSelect';
   modeSelect.onchange = function() {
      var content = document.getElementById('content');
      content.mode = this.options[this.selectedIndex].value;
      if (content.model);
         renderElement(content.model);
      repaint();
   };
   var standardModeOption = document.createElement('option');
   standardModeOption.value = 'standard';
   if (content.mode=='standard')
      standardModeOption.selected = 'true';
   standardModeOption.appendChild(document.createTextNode('Standard'));
   modeSelect.appendChild(standardModeOption);
   var expertModeOption = document.createElement('option');
   expertModeOption.value = 'expert';
   if (content.mode=='expert')
      expertModeOption.selected = 'true';
   expertModeOption.appendChild(document.createTextNode('Expert'));
   modeSelect.appendChild(expertModeOption);
   var allModeOption = document.createElement('option');
   allModeOption.value = '';
   if (content.mode=='')
      allModeOption.selected = 'true';
   allModeOption.appendChild(document.createTextNode('All'));
   modeSelect.appendChild(allModeOption);
   modeItem.appendChild(modeSelect);
   tabul.appendChild(modeItem);
   return viewbar;
}

function createActionBar()
{
   if (actionbar) return actionbar;
   // lazy initialize
   actionbar = document.createElement('div');
   actionbar.id = 'actionbar';
   var backButton = document.createElement('div');
   backButton.id = 'backButton';
   backButton.className = 'backDisabled';
   backButton.title = 'Back';
   backButton.onclick = function() {
      back();
   };
   actionbar.appendChild(backButton);
   var forwardButton = document.createElement('div');
   forwardButton.id = 'forwardButton';
   forwardButton.className = 'forwardDisabled';
   forwardButton.title = 'Forward';
   forwardButton.onclick = function() {
      forward();
   };
   actionbar.appendChild(forwardButton);
   var selectTreeButton = document.createElement('div');
   selectTreeButton.id = 'selectTreeButton';
   selectTreeButton.className = 'selectTreeButton';
   selectTreeButton.title = 'Select in Containment Tree';
   selectTreeButton.onclick = function() {
      var content = document.getElementById('content');
      if (content.model);
      {
	      var node = findNode(content.model.getAttribute('id'));
	      if (node)
	         selectNode(node);
         else
            alert('Selected node is not appearing in containment tree');
      }
   };
   actionbar.appendChild(selectTreeButton);
   return actionbar;
}
/**
 * Value node renderer
 * @param value a HTML element containing value
 * @param element DOM element
 */
function renderValueNode(value, element)
{
   var text = nodeValue(element);
   renderValueText(value, text);
}
/**
 * Value text renderer
 * @param value a HTML element containing value
 * @param text text to display
 */
function renderValueText(value, text)
{
   if (text && text.indexOf('<html>')>=0)
   {
      var startBodyIndex = text.indexOf('<body>');
      var endBodyIndex = text.indexOf('</body>', startBodyIndex);
      if (startBodyIndex>0 && endBodyIndex > 0)
      {
         var htmlContent = text.substring(startBodyIndex, endBodyIndex);
         if (htmlContent.indexOf('mdel://') >= 0)
         {
            var reg = new RegExp('(<\s*a.+)href\s*=\s*\"(mdel://)(.*)\"(.*>)', 'gi');
            htmlContent = htmlContent.replace(reg, "$1href=\"javascript:showSpec('$3')\"$4");
         }
         value.innerHTML = htmlContent;
      }
   }
   else
   {
      var tokens = (' ' + text).split(/(\r\n|[\r\n])/g);
      if (tokens.length > 1)
      {
         for (var t=0; t<tokens.length; t++)
         {
            renderValueLink(value, tokens[t]);
            value.appendChild(document.createElement('br'));
         }
      }
      else
      {
         renderValueLink(value, tokens[0]);
      }
   }
}

/**
 * Value link renderer
 * @param value a HTML element containing value
 * @param text text containing link
 */
function renderValueLink(value, text)
{
   if (text.indexOf('http://')==1)
   {
      var anchor = document.createElement('a');
      anchor.href = text;
      anchor.target = '_blank';
      anchor.appendChild(document.createTextNode(text));
      value.appendChild(anchor);
   }
   else if (text.indexOf('file://')==1)
   {
      var url = trim(text).substring(7);
      if (url.length >= 4)
      {
         var endsWith = url.substring(url.length - 4).toLowerCase();
         if (endsWith == '.flv' || endsWith == '.mp4')
         {
            var container = document.createElement('div');
            container.style.height = '344px';
            container.style.width = '480px';
            var embed = document.createElement('embed');
            embed.src = resourcesLocation + 'swf/WebVideo.swf';
            embed.type = 'application/x-shockwave-flash';
            embed.width = '100%';
            embed.height = '100%';
            embed.setAttribute('flashvars',  'url=../../' + url);
            embed.setAttribute('quality', 'high');
            container.appendChild(embed);
            value.appendChild(container);
         }
         else if (endsWith == '.mp3')
         {
            var container = document.createElement('div');
            container.style.height = '24px';
            container.style.width = '320px';
            var embed = document.createElement('embed');
            embed.src = resourcesLocation + 'swf/WebAudio.swf';
            embed.type = 'application/x-shockwave-flash';
            embed.width = '100%';
            embed.height = '100%';
            embed.setAttribute('flashvars', 'url=' + url);
            embed.setAttribute('quality', 'high');
            value.appendChild(embed);
            container.appendChild(embed);
            value.appendChild(container);
         }
         else if (endsWith == '.swf')
         {
            var container = document.createElement('div');
            container.style.height = '344px';
            container.style.width = '480px';
            var embed = document.createElement('embed');
            embed.src = url;
            embed.type = 'application/x-shockwave-flash';
            embed.width = '100%';
            embed.height = '100%';
            embed.setAttribute('quality', 'high');
            container.appendChild(embed);
            value.appendChild(container);
         }
         else
         {
            var pathOffset = url.lastIndexOf('/');
            var name = url;
            if (pathOffset >= 0)
               name = url.substring(pathOffset + 1);
            var anchor = document.createElement('a');
            anchor.href = url;
            anchor.target = '_blank';
            anchor.appendChild(document.createTextNode(name));
            value.appendChild(anchor);
         }
      }
   }
   else
      value.appendChild(document.createTextNode(text));
}
/**
 * Render browser tree
 * @param responseXML a xml
 */
function renderBrowser(responseXML)
{
   var magicdraw;
   if (responseXML)
      magicdraw = responseXML.getElementsByTagName('magicdraw')[0];
   if (magicdraw != null)
   {
      showLoading();
      var root = document.createElement('ul');
      root.id = 'tree';
      tree = new Tree(root.id);
      tree.image.plus = resourcesLocation + 'images/tree/plus.gif';
      tree.image.minus = resourcesLocation + 'images/tree/minus.gif';
      tree.root = root;
      var dataModel = firstChild(magicdraw);
      var node = addNode(root, dataModel.getAttribute('name'), "javascript: showSpec('"+dataModel.getAttribute('refid')+"');", dataModel.getAttribute('icon'));
      node.data = dataModel;
      node.setAttribute('refid', dataModel.getAttribute('refid'));
      buildTree(node);
      var browser = document.getElementById('browser');
      browser.appendChild(root);
      tree.expand(node);
      hideLoading();
   }
}
var usehyperlink = false;
var useStack = false;
/**
 * Render model
 * @param responseXML a xml
 */
function renderModel(responseXML)
{
   var magicdraw;
   if (responseXML)
      magicdraw = responseXML.getElementsByTagName('magicdraw')[0];
   if (magicdraw == null)
   {
      alert('This element was not generated from project.');
      return;
   }
   var model = firstChild(magicdraw);
   // validate hyperlinkModelActive
   var stopRender = false;
   if (model.hasChildNodes && usehyperlink)
   {
      var childNodes = model.childNodes;
      for (var c=0; c<childNodes.length && !stopRender; c++)
      {
         // Stereotype
         if (childNodes[c].tagName == 'appliedStereotype')
         {
            if (childNodes[c].hasChildNodes)
            {
               var stereotypes = childNodes[c].childNodes;
               for (var s=0; s<stereotypes.length && !stopRender; s++)
               {
                  var stereotypeName = stereotypes[s].getAttribute('name');
                  if (stereotypeName == 'HyperlinkOwner')
                  {
                     if (stereotypes[s].hasChildNodes)
                     {
                        var properties = stereotypes[s].childNodes;
                        for (var p=0; p<properties.length && !stopRender; p++)
                        {
                           var propertyName = properties[p].getAttribute('name');
                           if (propertyName == 'hyperlinkModelActive');
                           {
                              if (properties[p].hasChildNodes)
                              {
                                 var elements = properties[p].childNodes;
                                 for (var e=0; e<elements.length && !stopRender; e++)
                                 {
                                    var refid = elements[e].getAttribute('refid');
                                    if (refid)
                                    {
                                       usehyperlink = false;
                                       showSpec(refid);
                                       stopRender = true;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
   if (!stopRender)
   {
      var content = document.getElementById('content');
      if (model.tagName=='diagram')
      {
         renderDiagram(model);
         selectView('diagram');
      }
      else
      {
         renderElement(model);
         var diagramtab = document.getElementById('diagramtab');
         if (diagramtab)
            diagramtab.style.display = 'none';
         selectView('specification');
      }
      var modelId = model.getAttribute('id');
      if (modelId)
      {
         if (backStack.peek() != modelId)
         {
            backStack.push(modelId);
            if (backStack.size()>1)
            {
               var backButton = document.getElementById('backButton');
               backButton.className = 'back';
            }
         }
         currentPageId = modelId;
         if (!useStack)
         {
            forwardStack.clear();
            var forwardButton = document.getElementById('forwardButton');
            forwardButton.className = 'forwardDisabled';
         }
      }
      repaint();
   }
}

/**
 * Element specification renderer.
 */
function renderElement(model)
{
   var content = document.getElementById('content');
   content.model = model;
   removeAll(content);
   content.appendChild(createActionBar());
   content.appendChild(createViewBar(model));
   var header = document.createElement('h2');
   header.id = 'contentHeader';
   header.appendChild(document.createTextNode(model.getAttribute('humanType')));
   if (navigator.userAgent.indexOf('MSIE 6')>=0)
      header.style.marginTop = '2em';
   content.appendChild(header);
   if (model.hasChildNodes)
   {
      var table = document.createElement('div');
      table.className = 'table';
      var thead = document.createElement('div');
      thead.className = 'thead';
      thead.appendChild(document.createTextNode('General Information'));
      table.appendChild(thead);
      var tbody = document.createElement('div');
      tbody.id = 'generalTable';
      tbody.className = 'tbody';
      table.appendChild(tbody);
      content.appendChild(table);
      var childNodes = model.childNodes;
      for (var c=0; c<childNodes.length; c++)
      {
         if (childNodes[c].tagName == 'name')
            header.appendChild(document.createTextNode(' ' + nodeValue(childNodes[c])));
         if (childNodes[c].nodeType==1)
         {
            // Stereotype
            if (childNodes[c].tagName == 'appliedStereotype')
            {
               if (childNodes[c].hasChildNodes)
               {
                  var stereotypes = childNodes[c].childNodes;
                  for (var s=0; s<stereotypes.length; s++)
                  {
                     var stable = document.createElement('div');
                     stable.className = 'table';
                     var sthead = document.createElement('div');
                     sthead.className = 'thead';
                     sthead.appendChild(document.createTextNode(stereotypes[s].getAttribute('humanType') + ' ' +stereotypes[s].getAttribute('name')));
                     stable.appendChild(sthead);
                     var stbody = document.createElement('div');
                     stbody.id = stereotypes[s].getAttribute('refid');
                     stbody.className = 'tbody';
                     stable.appendChild(stbody);
                     content.appendChild(stable);
                     var properties = stereotypes[s].childNodes;
                     for (var p=0; p<properties.length; p++)
                     {
                        if (properties[p].firstChild && properties[p].firstChild.nodeType==1)
                        {
                           var row = document.createElement('div');
                           row.className = 'row';
                           var label = document.createElement('label');
                           label.appendChild(document.createTextNode(properties[p].getAttribute('humanName')));
                           row.appendChild(label);
                           var separator = document.createElement('span');
                           separator.appendChild(document.createTextNode(' : '));
                           row.appendChild(separator);
                           var value = document.createElement('span');
                           var collections = properties[p].childNodes;
                           for (var o=0; o<collections.length; o++)
                           {
                              if (collections[o].getAttribute('refid'))
                                 createLink(value, collections[o]);
                              else
                                 renderValueNode(value, collections[o]);
                              value.appendChild(document.createElement('br'));
                           }
                           row.appendChild(value);
                           stbody.appendChild(row);
                        }
                     }
                  }
               }
            }
            else if (childNodes[c].tagName == 'documentation')
            {
               var dtable = document.createElement('div');
               dtable.className = 'table';
               var dthead = document.createElement('div');
               dthead.className = 'thead';
               dthead.appendChild(document.createTextNode(childNodes[c].getAttribute('humanName')));
               dtable.appendChild(dthead);
               var dtbody = document.createElement('div');
               dtbody.id = 'documentationTable';
               dtbody.className = 'tbody';
               dtable.appendChild(dtbody);
               content.appendChild(dtable);
               var row = document.createElement('div');
               row.className = 'row';
               var value = document.createElement('span');
               renderValueNode(value, childNodes[c]);
               row.appendChild(value);
               dtbody.appendChild(row);
               continue;
            }
            // General Information
            if (childNodes[c].tagName == 'map')
               continue;
            var showProperty = false;
            if (content.mode == '')
               showProperty = true;
            else
            {
               var mode = childNodes[c].getAttribute('mode');
               if (mode)
                  showProperty = mode.indexOf(content.mode) >= 0;
            }
            if (showProperty)
            {
               var row = document.createElement('div');
               row.className = 'row';
               var label = document.createElement('label');
               label.appendChild(document.createTextNode(childNodes[c].getAttribute('humanName')));
               row.appendChild(label);
               var separator = document.createElement('span');
               separator.appendChild(document.createTextNode(' : '));
               row.appendChild(separator);
               var value = document.createElement('span');
               if (childNodes[c].firstChild && childNodes[c].firstChild.nodeType==1)
               {
                  var collections = childNodes[c].childNodes;
                  var cdiv = document.createElement('div');
                  cdiv.className = 'none';
                  cdiv.id = childNodes[c].getAttribute('humanName');
                  if (collections.length>1)
                  {
                     var img = document.createElement('img');
                     img.src = Content.imgShow;
                     img.alt = '';
                     img.className = 'toggle';
                     img.contentId = cdiv.id;
                     img.onclick = function()
                     {
                        var content = new Content();
                        content.imgHide = resourcesLocation + 'images/left_triangle.gif';
                        content.showHide(this, this.contentId);
                     };
                     row.appendChild(img);
                  }
                  value.appendChild(cdiv);
                  for (var o=0; o<collections.length; o++)
                  {
                     var humanType = collections[o].getAttribute('humanType');
                     if (humanType)
                        createLink(cdiv, collections[o]);
                     else
                        renderValueNode(cdiv, collections[o]);
                     cdiv.appendChild(document.createElement('br'));
                  }
               }
               else
               {
                  var humanType = childNodes[c].getAttribute('humanType');
                  if (humanType)
                     createLink(value, childNodes[c]);
                  else
                     renderValueNode(value, childNodes[c]);
               }
               row.appendChild(value);
               tbody.appendChild(row);
            }
         }
      }
   }
}
/**
 * Diagram specification renderer.
 */
function renderDiagram(model)
{
   var content = document.getElementById('content');
   content.model = model;
   removeAll(content);
   content.appendChild(createActionBar());
   content.appendChild(createViewBar(model));
   var header = document.createElement('h2');
   header.id = 'contentHeader';
   header.appendChild(document.createTextNode(model.getAttribute('diagramType')));
   if (navigator.userAgent.indexOf('MSIE 6')>=0)
      header.style.marginTop = '2em';
   content.appendChild(header);
   var mapName;
   if (model.hasChildNodes)
   {
      mapName = 'map_' + model.getAttribute('id');
      var childNodes = model.childNodes;
      for (var c=0; c<childNodes.length; c++)
      {
         if (childNodes[c].tagName == 'name')
            header.appendChild(document.createTextNode(' ' + nodeValue(childNodes[c])));
         else if (childNodes[c].tagName == 'map')
         {
            var map = document.createElement('map');
            map.id = mapName;
            map.setAttribute('name', mapName);
            if (childNodes[c].hasChildNodes)
            {
               var areas = childNodes[c].childNodes;
               for (var a=0; a<areas.length; a++)
               {
                  var area = document.createElement('area');
                  area.shape = 'poly';
                  area.alt = areas[a].getAttribute('name');
                  if (useElementLink)
                     area.href = "javascript: showElementLink('"+areas[a].getAttribute('refid')+"');";
                  else
                     area.href = "javascript: showSpec('"+areas[a].getAttribute('refid')+"');";
                  var points = areas[a].childNodes;
                  var coordsString = "";
                  for (var p=0; p<points.length; p++, coordsString += ',')
                  {
                     coordsString += points[p].getAttribute('x') + ',' + points[p].getAttribute('y');
                  }
                  coordsString += points[0].getAttribute('x') + ',' + points[0].getAttribute('y');
                  area.coords = coordsString;
                  map.appendChild(area);
               }
            }
            content.appendChild(map);
         }
      }
   }
   var diagramContainer = document.createElement('div');
   diagramContainer.id = 'diagramContainer';
   var image = document.createElement('img');
   image.src = model.getAttribute('src');
   image.width = model.getAttribute('width');
   image.height = model.getAttribute('height');
   image.alt = ''; // model.getAttribute('diagramType');
   image.border = '0';
   image.className = 'diagram';
   if (mapName)
      image.useMap = '#' + mapName;
   diagramContainer.appendChild(image);
   content.appendChild(diagramContainer);
}
/**
 * Show element linked page
 * @elementId element id
 */
function showElementLink(elementId, keepStack)
{
   usehyperlink = true;
   useStack = keepStack;
   XML.load(resourcesLocation + '/xml/' + elementId + '.xml', renderModel);
}
/**
 * Show element's specification page
 * @elementId element id
 */
function showSpec(elementId, keepStack)
{
   usehyperlink = false;
   useStack = keepStack;
   XML.load(resourcesLocation + 'xml/' + elementId + '.xml', renderModel);
}

/**
 * Expand node path
 */
function expandPath(nodePath)
{
   stopexpand = false;
   var rootTree = document.getElementById(tree.treeId);
   var path = nodePath.length - 1;
   internalExpandPath(rootTree, nodePath, path);
}
/** we already found node stop searching and collapse investigating node */
var stopexpand = false;
function internalExpandPath(rootNode, nodePath, path)
{
   if (path < 0 || stopexpand) return;
   var childNodes = rootNode.childNodes;
   var content = document.getElementById('content');
   if (childNodes)
   {
      for (var c=0; c<childNodes.length; c++)
      {
         if (childNodes[c].tagName == 'UL')
            internalExpandPath(childNodes[c], nodePath, path);
         else if (childNodes[c].tagName == 'LI')
         {
            var refid = childNodes[c].getAttribute('refid');
            childNodes[c].internalExpand = false;
            if (refid == 'relations')
            {
               // if node already expand, left it expand
               if (childNodes[c].lastChild && !childNodes[c].lastChild.isExpanded)
               {
                  tree.expand(childNodes[c]);
                  // mark as node is expanded for investigating
                  childNodes[c].internalExpand = true;
               }
               internalExpandPath(childNodes[c], nodePath, path);
            }
            else if (refid == nodePath[path])
            {
               // if node already expand, left it expand
               if (childNodes[c].lastChild && !childNodes[c].lastChild.isExpanded)
               {
                  tree.expand(childNodes[c]);
                  // mark as node is expanded for investigating
                  childNodes[c].internalExpand = true;
               }
               internalExpandPath(childNodes[c], nodePath, --path);
            }
            // if found target node then stop expanding
            if (path == -1)
               stopexpand = true;
            if (!stopexpand)
            {
               // collapse node that use for investigating.
               if (childNodes[c].internalExpand)
                  tree.collapse(childNodes[c]);
            }
         }
      }
   }
}

/**
 * Goto last visit page
 */
function back()
{
   if (backStack.size()==1) return;
   backStack.pop();
   var lastPageId = backStack.pop();
   if (backStack.size()<1)
   {
      var backButton = document.getElementById('backButton');
      backButton.className = 'backDisabled';
   }
   if (lastPageId)
   {
      forwardStack.push(currentPageId);
      var forwardButton = document.getElementById('forwardButton');
      forwardButton.className = 'forward';
      showSpec(lastPageId, true);
      var node = findNode(lastPageId);
      if (node)
         selectNode(node);
   }
}

/**
 * Forward to next visit page
 */
function forward()
{
   var lastPageId = forwardStack.pop();
   if (forwardStack.size()<1)
   {
      var forwardButton = document.getElementById('forwardButton');
      forwardButton.className = 'forwardDisabled';
   }
   if (lastPageId)
   {
      showSpec(lastPageId, true);
      var node = findNode(lastPageId);
      if (node)
         selectNode(node);
   }
}

/**
 * Trim string
 * @param input string
 * @return output string
 */
function trim(aB){return aB.replace(/^\s*|\s*$/g,'');}
/**
 * Test matches node with regular expression
 */
function match(node, attr, regx, searchResults)
{
   if (node.tagName == 'ownedDiagram')
      return;
   if (node.nodeType==1)
   {
      var name = node.getAttribute(attr);
      if ((name=='' || name) && regx.test(name))
         searchResults[searchResults.length] = node;
   }
   if (node.hasChildNodes())
   {
      var childNodes = node.childNodes;
      for (var i=0; i<childNodes.length; i++)
         match(childNodes[i], attr, regx, searchResults);
   }
}
/**
 * Search function. Regular expression can be used with element name.
 * @param elementName name of element being search.
 */
function search(elementName)
{
   if (tree.root)
   {
      showLoading();
      var dataModel = tree.root.firstChild.data;
      var regx
      try
      {
         regx = new RegExp(trim(elementName), 'i');
      }
      catch (e)
      {
         alert('Invalid search pattern \nReason: ' + e.message + '\nPlease validate regular expression syntax in search text');
      }
      if (regx)
      {
         var searchResults = new Array(0);
         match(dataModel, 'name', regx, searchResults);
         var content = document.getElementById('content');
         removeAll(content);
         var header = document.createElement('h2');
         header.id = 'contentHeader';
         header.appendChild(document.createTextNode('Search Results'));
         content.appendChild(header);
         if (searchResults.length > 0)
         {
            var stable = document.createElement('div');
            stable.className = 'table';
            var sthead = document.createElement('div');
            sthead.className = 'thead';
            sthead.appendChild(document.createTextNode('Search Results'));
            stable.appendChild(sthead);
            var stbody = document.createElement('div');
            stbody.className = 'tbody';
            stable.appendChild(stbody);
            content.appendChild(stable);
            for (var p=0; p<searchResults.length; p++)
            {
               if (searchResults[p].nodeType==1)
               {
                  var row = document.createElement('div');
                  row.className = 'row';
                  var name = document.createElement('span');
                  name.style.verticalAlign = 'middle';
                  createLink(name, searchResults[p]);
                  row.appendChild(name);
                  var type = document.createElement('span');
                  type.style.verticalAlign = 'middle';
                  type.appendChild(document.createTextNode(searchResults[p].getAttribute('humanType')));
                  row.appendChild(type);
                  stbody.appendChild(row);
               }
            }
            repaint();
         }
         else
         {
            var message = document.createElement('h5');
            message.style.padding = '1em';
            message.appendChild(document.createTextNode('No element name containing all your search terms were found.'));
            content.appendChild(message);
         }
      }
      hideLoading();
   }
};
