package com.idega.block.navigation.presentation;

import com.idega.presentation.Table;
import com.idega.presentation.IWContext;
import com.idega.presentation.Block;
import com.idega.presentation.text.Link;
import com.idega.builder.business.PageTreeNode;
import com.idega.builder.business.BuilderLogic;
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

  public NavigationMenu() {

  }

  public void main(IWContext iwc){
    Table T = new Table();
    T.setCellpadding(0);
    T.setCellspacing(0);
    int row = 1,col = 1;

    PageTreeNode node = new PageTreeNode(rootNode, iwc, PageTreeNode.PAGE_TREE);
    Iterator I = node.getChildren();
    Link L;
    if(I!=null){
      while(I.hasNext()){
        PageTreeNode n = (PageTreeNode) I.next();
        L = new Link(n.getNodeName(),BuilderLogic.getIBPageURL(n.getNodeID()));
        T.add(L,col,row);
        switch (viewType) {
          case VERTICAL:  row++;        break;
          case HORIZONTAL: col++;       break;
        }
      }
    }
    L = new Link(node.getNodeName(),BuilderLogic.getIBPageURL(node.getNodeID()));
    T.add(L,col,row);
    add(T);
  }

  public void setViewType(int type){
    viewType = type;
  }

  public void setHorizontal(){
    viewType = HORIZONTAL ;
  }

  public void setVertical(){
    viewType = VERTICAL;
  }

  public void setRootNode(int rootId){
    rootId  = rootId;
  }
}