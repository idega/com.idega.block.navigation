package com.idega.block.navigation.presentation;

import com.idega.presentation.Table;
import com.idega.presentation.IWContext;
import com.idega.presentation.Block;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.builder.business.PageTreeNode;
import com.idega.builder.business.BuilderLogic;
import com.idega.builder.data.IBPage;
import java.util.Iterator;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

public class NavigationMenu extends Block {

  private final static int VERTICAL = 1,HORIZONTAL = 2;
  private int viewType = 1;
  private int rootNode = 1;

  private IWBundle iwb;
  private IWResourceBundle iwrb;
  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.navigation";

  private String fontColor = "#000000";
  private int fontSize = 2;
  private String bgrColor = "#FFFFFF";
  private String width = null;
  private String height = null;

  private boolean asTab = false;
  private boolean asButton = false;
  private boolean asFlipped = false;

  public NavigationMenu() {

  }

  public void main(IWContext iwc){
    Table T = new Table();
    T.setCellpadding(0);
    T.setCellspacing(0);
    if(width != null){
      T.setWidth(width);
    }
    if(height != null){
      T.setHeight(height);
    }
    T.setColor(bgrColor );

    int row = 1,col = 1;

    PageTreeNode node = new PageTreeNode(rootNode, iwc, PageTreeNode.PAGE_TREE);
    Iterator I = node.getChildren();
    Link L;
    if(I!=null){
      while(I.hasNext()){
        PageTreeNode n = (PageTreeNode) I.next();
        L = getLink(n.getNodeName(),n.getNodeID());
        T.add(L,col,row);
        switch (viewType) {
          case VERTICAL:  row++;        break;
          case HORIZONTAL: col++;       break;
        }
      }
    }
    L = getLink(node.getNodeName(),node.getNodeID());
    T.add(L,col,row);
    add(T);
  }

  private Link getLink(String text, int PageId){
    Text t = new Text(text);
    t.setFontColor(fontColor);
    t.setFontSize(fontSize);
    Link L = new Link();
    L.setText(text);
    L.setPage(PageId);
    if(asButton){
      L.setAsImageButton( asButton);
    }
    else if(asTab){
      L.setAsImageTab(asTab,asFlipped );
    }
    else{
      L.setFontColor(fontColor);
      L.setFontSize(fontSize);
    }
    return L;
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

  public void setBackgroundColor(String color){
    bgrColor = color;
  }

  public void setWidth(String width){
    width = width;
  }

  public void setHeight(String height){
    height = height;
  }

  public void setAsButtons(boolean asButtons){
    asButton = asButtons;
  }
  public void setAsTabs(boolean asTabs,boolean Flipped){
    asTab = asTabs;
    asFlipped = Flipped ;
  }
}