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
import com.idega.builder.handler.HorizontalVerticalViewHandler;
import com.idega.builder.handler.VerticalAlignmentHandler;
import com.idega.builder.handler.HorizontalAlignmentHandler;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
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

  private String fontColor = "#000000";
  private int fontSize = 2;
  private String bgrColor = "#FFFFFF";
  private String width = null;
  private String height = null;
	private String fontStyle = "";
  private int cellPadding = 0;

  private boolean asTab = false;
  private boolean asButton = false;
  private boolean asFlipped = false;
	private boolean withRootAsHome = true;
	private String HomeVerticalAlignment = VerticalAlignmentHandler.BOTTOM;
	private String HomeHorizontalAlignment = HorizontalAlignmentHandler.RIGHT;

  public NavigationMenu() {

  }

  public void main(IWContext iwc){
    Table T = new Table();
    T.setCellpadding(cellPadding);
    T.setCellspacing(0);
    if(width != null){
      T.setWidth(width);
    }
    if(height != null){
      T.setHeight(height);
    }
    T.setColor(bgrColor );

		Link L;
		PageTreeNode node = new PageTreeNode(rootNode, iwc);
    int row = 1,col = 1;
		boolean bottom = !HomeVerticalAlignment.equals(VerticalAlignmentHandler.TOP);
		boolean left = !HomeHorizontalAlignment.equals(HorizontalAlignmentHandler.RIGHT);
		boolean vertical = viewType == VERTICAL;

		if(withRootAsHome){
			L = getLink(node.getNodeName(),node.getNodeID());
		  if(vertical && !bottom){
			  T.add(L,col,row++);
				withRootAsHome = false;
		  }
			else if(!vertical && left){
			  T.add(L,col++,row);
				withRootAsHome = false;
			}
		}

    Iterator I = node.getChildren();

    if(I!=null){
      while(I.hasNext()){
        PageTreeNode n = (PageTreeNode) I.next();
        L = getLink(n.getNodeName(),n.getNodeID());
        T.add(L,col,row);
        if(vertical)
					row++;
				else
					col++;
      }
    }
		if(withRootAsHome){
		  if(bottom || !left){
				L = getLink(node.getNodeName(),node.getNodeID());
				T.add(L,col,row);
			}
		}
    add(T);
  }

  private Link getLink(String text, int PageId){
    Text T = new Text(text);
      T.setFontColor(fontColor);
      T.setFontSize(fontSize);
			if(! "".equals(fontStyle)){
			  T.setFontStyle(fontStyle);
			}
		Link L  = new Link(T);
    L.setPage(PageId);
    if(asButton){
      L.setAsImageButton( asButton);
    }
    else if(asTab){
      L.setAsImageTab(asTab,asFlipped );
    }
		System.err.println("node nr "+PageId);
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

	public void setFontStyle(String style){
    fontStyle = style;
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

	public void setUseRootAsHome(boolean useRootAsHome){
		withRootAsHome = useRootAsHome;
	}

  public void setPadding(int padding) {
    cellPadding = padding;
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
}