# InterestView  
  模仿探探/Like等部分社交软件的寻找感兴趣的好友功能

![InterestView](https://raw.githubusercontent.com/JeremyLam1/InterestView/master/InterestView.gif)

### 左右切换监听事件：
```
ivList.setiDirectionListener(new InterestView.IDirectionListener() {
            @Override
            public void onDirection(boolean isLeft) {
                Toast.makeText(MainActivity.this, isLeft ? "踩" : "赞", Toast.LENGTH_SHORT).show();
            }
        });
```

### 左按钮点击事件：
```
ivList.setiLeftBtnOnclickListener(new InterestView.ILeftBtnOnclickListener() {
            @Override
            public void onclick() {
                // TODO: 2016/12/21 .... 
            }
        });
```

### 右按钮点击事件：
```
ivList.setiRightBtnOnclickListener(new InterestView.IRightBtnOnclickListener() {
            @Override
            public void onclick() {
                // TODO: 2016/12/21 ....
            }
        });
```

