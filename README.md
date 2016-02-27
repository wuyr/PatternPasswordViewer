# PatternPasswordViewer
PatternPasswordViewer
一个可以在android手机上查看本机的图案锁屏密码，需要root权限。
原理：读取手机上的图案密码文件，转回SHA1码后作为key将数据库的value查询出来，然后通过自定义的view画出密码的路径。
参考资料：http://www.myhack58.com/Article/html/3/92/2013/38772.htm
http://www.codesec.net/view/57893.html
