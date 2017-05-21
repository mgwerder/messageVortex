Important Notice
================
Ths is a development repository. All sources in this repository should be regarded with extreme distrust as they are under heavy construction.

Troubleshooting
===============


Encryption fails with longer keylengths
---------------------------------------
Make sure you installed the ulimmited strength JCE in your JRE


Application fails with a security provider Exception
----------------------------------------------------
Most likely the bouncycastle jars are not included in the classpath