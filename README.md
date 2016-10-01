# LowPoly

with high performance, render picture within seconds

####intro
[http://www.jianshu.com/p/6660809b6375](http://www.jianshu.com/p/6660809b6375)

####Usage
```java
...
{
...
    LowPoly.createLowPoly(this, bitmapOriginal, accuracy, RENDERED_FLAG);

}


static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RENDERED_FLAG:
                    ivOut.setImageBitmap(LowPoly.bmpRendered);
                    break;
            }
        }
};
...
```

![](app/demo_show.gif)
