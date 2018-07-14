# SmartTable
a custom table for show datas by pages

This is a custom table for show datas by pages. Cells are extended by text length. All columns can be shown by slide horizontally.
For change page can set page index for table.

一个自定义表格，分页展示，可以按照文本长度扩展，左右滑动显示全部列，通过改变页码支持上下翻页。

api supported:

```java
//set text color
public void setTextColor(int textColor);

//set table head color
public void setTitleColor(int titleColor);

//set cell width limit
public void setMaxCellWidth(int width);

//set copy cell background
public void setCopyColor(int color);

//set table border color
public void setLineColor(int lineColor);

//set text padding in cell
public void setPadding(int padding);

//set cell height limit
public void setRowHeight(int height);

//set text size
public void setTextSize(int size);

//set border width
public void setBorder(int border);

//if show copy cell
public void setShowCopy(boolean copy);

//set data to show
public void setData(final List<List<String>> datas, final String[] titles);

//to next page
public void nextPage();

//to previous page
public void previous();
```

![image](https://github.com/funny9527/SmartTable/blob/master/device-2018-07-14-104216.png)
![image](https://github.com/funny9527/SmartTable/blob/master/device-2018-07-14-104249.png)
![image](https://github.com/funny9527/SmartTable/blob/master/device-2018-07-14-104309.png)

