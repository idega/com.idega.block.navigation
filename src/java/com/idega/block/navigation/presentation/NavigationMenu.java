package com.idega.block.navigation.presentation;

import com.idega.presentation.Table;
import com.idega.presentation.Image;
import com.idega.presentation.IWContext;
import com.idega.presentation.Block;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.text.TextStyler;
import com.idega.util.text.StyleConstants;
import com.idega.builder.business.PageTreeNode;
import com.idega.builder.business.BuilderLogic;
import com.idega.builder.data.IBPage;
import java.util.Iterator;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.builder.handler.HorizontalVerticalViewHandler;
import com.idega.builder.handler.VerticalAlignmentHandler;
import com.idega.builder.handler.HorizontalAlignmentHandler;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a> & <a href="mailto:laddi@idega.is">Thorhallur Helgason</a>
 * @version 1.0
 */

public class NavigationMenu extends Block {

  private final static int VERTICAL = HorizontalVerticalViewHandler.VERTICAL,
                            HORIZONTAL = HorizontalVerticalViewHandler.HORIZONTAL;

  private int viewType = 1;
  private int rootNode = 1;

  private IWBundle iwb;
  private IWResourceBundle iwrb;
  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.navigation";

  private int fontSize = 2;
  private String fontColor = "#000000";
  private String bgrColor = "#FFFFFF";
  private String higligtFontColor = "#000000";
  private String higligtBgrColor = "#FFFFFF";
  private String tableBackGroundColor = null;
  private String width = null;
  private String height = null;

  private boolean _styles = true;
  private String _name;
  private String _hoverName;
  private String fontStyle;

  private Image _iconImage;
  private Image _iconOverImage;
  private Image _spacer;
  private int _widthFromIcon = 5;

  private int cellPadding = 0;
  private int cellSpacing = 0;
  private int currentPageId = -1;

  private boolean asTab = false;
  private boolean asButton = false;
  private boolean asFlipped = false;
  private boolean withRootAsHome = true;
  private String HomeVerticalAlignment = VerticalAlignmentHandler.BOTTOM;
  private String HomeHorizontalAlignment = HorizontalAlignmentHandler.RIGHT;

  public NavigationMenu() {
  }

  public void main(IWContext iwc){
    setStyles();
    String sCurrentPageId = iwc.getParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
    int currentPageId = sCurrentPageId !=null ? Integer.parseInt(sCurrentPageId):-1;

    PageTreeNode node = new PageTreeNode(rootNode, iwc);
    Iterator I = node.getChildren();

    boolean bottom = !HomeVerticalAlignment.equals(VerticalAlignmentHandler.TOP);
    boolean left = !HomeHorizontalAlignment.equals(HorizontalAlignmentHandler.RIGHT);
    boolean vertical = viewType == VERTICAL;
    int row = 1,col = 1;

    Table T = new Table();
    T.setCellpadding(cellPadding);
    T.setCellspacing(cellSpacing);
    if(tableBackGroundColor !=null)
      T.setColor(tableBackGroundColor);
    if(width != null)
      T.setWidth(width);
    if(height != null)
      T.setHeight(height);

    Link L = null;
    Text text = null;
    Image spacer = Table.getTransparentCell(iwc);
      spacer.setWidth(_widthFromIcon);

    if(withRootAsHome){
      L = getLink(node.getNodeName(),node.getNodeID(),currentPageId);
      if(vertical && !bottom){
        if ( _iconImage != null ) {
          Image image = new Image(_iconImage.getMediaServletString());
          if ( _iconOverImage != null )
            L.setOnMouseOverImage(image,_iconOverImage);
          T.add(image,col,row);
          T.add(spacer,col,row);
        }
        T.add(L,col,row++);
        withRootAsHome = false;
      }
      else if(!vertical && left){
        if ( _iconImage != null ) {
          Image image = new Image(_iconImage.getMediaServletString());
          if ( _iconOverImage != null )
            L.setOnMouseOverImage(image,_iconOverImage);
          T.add(image,col,row);
          T.add(spacer,col,row);
        }
        T.add(L,col++,row);
        if ( _spacer != null ) {
          T.add(_spacer,col++,row);
        }
        withRootAsHome = false;
      }
    }

    if(I!=null){
      while(I.hasNext()){
        PageTreeNode n = (PageTreeNode) I.next();
        L = getLink(n.getNodeName(),n.getNodeID(),currentPageId);
        if ( _iconImage != null ) {
          Image image = new Image(_iconImage.getMediaServletString());
          if ( _iconOverImage != null && L != null )
            L.setOnMouseOverImage(image,_iconOverImage);
          T.add(image,col,row);
          T.add(spacer,col,row);
        }
        T.add(L,col,row);
        if ( _spacer != null && !vertical && I.hasNext() ) {
          col++;
          T.add(_spacer,col,row);
        }
        if ( _spacer != null && !vertical && !I.hasNext() && withRootAsHome && !left ) {
          col++;
          T.add(_spacer,col,row);
        }
        if(vertical)
          row++;
        else
          col++;
      }
    }

    if(withRootAsHome){
      if(bottom || !left){
        L = getLink(node.getNodeName(),node.getNodeID(),currentPageId);
        if ( _iconImage != null ) {
          Image image = new Image(_iconImage.getMediaServletString());
          if ( _iconOverImage != null )
            L.setOnMouseOverImage(image,_iconOverImage);
          T.add(image,col,row);
          T.add(spacer,col,row);
        }
        T.add(L,col,row);
      }
    }

    add(T);
  }

  private Link getLink(String text, int PageId, int currentPageId){
    Link L  = new Link(text);
      if(_styles){
        if ( PageId == currentPageId )
          L.setStyle(_hoverName);
        else
          L.setStyle(_name);
      }
      else {
        L.setFontColor(currentPageId != PageId?fontColor:higligtFontColor);
        L.setFontSize(fontSize);
      }

    L.setPage(PageId);
    if(asButton){
      L.setAsImageButton( asButton,true);
    }
    else if(asTab){
      L.setAsImageTab(asTab,true,asFlipped );
    }

    return L;
  }

  private void setStyles() {
    if ( _name == null )
      _name = this.getName();
    if ( _name == null ) {
      if ( getICObjectInstanceID() != -1 )
        _name = "nav_"+Integer.toString(getICObjectInstanceID());
      else
        _name = "nav_"+Double.toString(Math.random());
    }
    _hoverName  = "hover_"+_name;

    if ( getParentPage() != null && fontStyle != null) {
      TextStyler styler = new TextStyler(fontStyle);
      styler.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION,StyleConstants.TEXT_DECORATION_UNDERLINE);

      getParentPage().setStyleDefinition("A."+_name+":link",fontStyle);
      getParentPage().setStyleDefinition("A."+_name+":visited",fontStyle);
      getParentPage().setStyleDefinition("A."+_name+":active",fontStyle);
      getParentPage().setStyleDefinition("A."+_name+":hover",styler.getStyleString());

      TextStyler styler2 = new TextStyler(fontStyle);
      if ( higligtFontColor != null )
        styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR,higligtFontColor);
      String style = styler2.getStyleString();

      getParentPage().setStyleDefinition("A."+_hoverName+":link",style);
      getParentPage().setStyleDefinition("A."+_hoverName+":visited",style);
      getParentPage().setStyleDefinition("A."+_hoverName+":active",style);

      styler2.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION,StyleConstants.TEXT_DECORATION_UNDERLINE);
      getParentPage().setStyleDefinition("A."+_hoverName+":hover",styler2.getStyleString());
    }
    else {
      _styles = false;
    }
  }

  public void setViewType(int type){
    viewType = type;
  }

  public void setHorizontal(boolean horizontal){
    if(horizontal)
    viewType = HORIZONTAL ;
  }

  public void setVertical(boolean vertical){
    if(vertical)
      viewType = VERTICAL;
  }

  public void setRootNode(IBPage page){
    rootNode = page.getID();
  }

  public void setRootNode(int rootId){
    rootNode  = rootId;
  }

  public void setFontColor(String color){
    fontColor = color;
  }

  public void setFontSize(int size){
    fontSize = size;
  }

  public void setFontStyle(String style){
    fontStyle = style;
  }

  public void setBackgroundColor(String color){
    bgrColor = color;
  }

  public void setTableBackgroundColor(String color){
    tableBackGroundColor = color;
  }

  public void setHighlightFontColor(String color){
    higligtFontColor = color;
  }

  public void setHighligtBackgroundColor(String color){
    higligtFontColor = color;
  }

  public void setWidth(String width){
    width = width;
  }

  public void setHeight(String height){
    height = height;
  }

  public void setUseRootAsHome(boolean useRootAsHome){
    withRootAsHome = useRootAsHome;
  }

  public void setPadding(int padding) {
    cellPadding = padding;
  }

  public void setSpacing(int spacing) {
    cellSpacing = spacing;
  }

  public void setHomeHorizontalAlignment(String align){
    if(align.equals(HorizontalAlignmentHandler.LEFT)||align.equals(HorizontalAlignmentHandler.RIGHT))
      HomeHorizontalAlignment = align;
  }

  public void setHomeVerticalAlignment(String align){
    if(align.equals(VerticalAlignmentHandler.BOTTOM)||align.equals(VerticalAlignmentHandler.TOP))
      HomeVerticalAlignment = align;
  }

  public void setAsButtons(boolean asButtons){
    asButton = asButtons;
  }

  public void setAsTabs(boolean asTabs,boolean Flipped){
    asTab = asTabs;
    asFlipped = Flipped ;
  }

  public void setIconImage(Image iconImage) {
    _iconImage = iconImage;
  }

  public void setIconOverImage(Image iconOverImage) {
    _iconOverImage = iconOverImage;
  }

  public void setWidthFromIcon(int widthFromIcon) {
    _widthFromIcon = widthFromIcon;
  }

  public void setSpacerImage(Image spacerImage) {
    _spacer = spacerImage;
  }

  public Object clone() {
    NavigationMenu obj = null;
    try {
      obj = (NavigationMenu) super.clone();

      if ( this._iconImage != null ) {
        obj._iconImage = (Image) this._iconImage.clone();
      }
      if ( this._iconOverImage != null ) {
        obj._iconOverImage = (Image) this._iconOverImage.clone();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace(System.err);
    }
    return obj;
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
}