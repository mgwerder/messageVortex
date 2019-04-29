<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1556182600279" ID="ID_1524815361" MODIFIED="1556185215531">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p style="text-align: left">
      Config file documentation
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556185000619" ID="ID_617935834" MODIFIED="1556185037654" POSITION="right" TEXT="[default]">
<node CREATED="1556184765454" ID="ID_665177606" MODIFIED="1556185064300" TEXT=" router_setup = ">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      References names of router sections within the config file.
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1556184908029" ID="ID_162769703" MODIFIED="1556185081408" TEXT="blender_setup =">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      References names of blender sections within the config file.
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1556184949813" ID="ID_859565383" MODIFIED="1556185102272" TEXT="transport_setup =">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      References names of transport sections within the config file.
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1556184884447" ID="ID_894812405" MODIFIED="1556185125762" TEXT="credential_source_setup =">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      References credential sources within the config file.
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node CREATED="1556182656403" ID="ID_188164378" MODIFIED="1556184833522" POSITION="right">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p style="text-align: left">
      router sections
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556183421738" ID="ID_1452890430" MODIFIED="1556183557185" TEXT="router_implementation =">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Reflects a java class implementing the &quot;Accountant&quot; interface
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556183574702" ID="ID_1461685106" MODIFIED="1556183680397" TEXT="net.messagevortex.router.SimpleRouterImplementation">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is a basic router implementation compatible with all operations
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node CREATED="1556183452017" ID="ID_1251069508" MODIFIED="1556183524798" TEXT="accounting_implementation =">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Reflects a java class implementing the &quot;Router&quot; interface
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556183598874" ID="ID_334893021" MODIFIED="1556183648354" TEXT="net.messagevortex.accounting.DummyAccountant">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is a dummy accountant allowing every transaction and acknowledging all requests without puzzles
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
</node>
<node CREATED="1556183053462" ID="ID_942202844" MODIFIED="1556184904665" POSITION="right">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p style="text-align: left">
      blender sections
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556183698817" ID="ID_142548317" MODIFIED="1556183833642" TEXT="blender_implementation =">
<node CREATED="1556183712470" ID="ID_1263822049" MODIFIED="1556183788003" TEXT="net.messagevortex.blender.InitialRecipesBlender">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is an initial blender. It converts the a plain mime encoded message sent by a traditional mail client to a vortex message.
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556184287275" ID="ID_1891727804" MODIFIED="1556184371062" TEXT="forward_transport = ">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      name of a transport configused for forwarding unblended messages.
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node CREATED="1556183834969" ID="ID_1624134071" MODIFIED="1556184096840" TEXT="net.messagevortex.blender.DummyBlender">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is the most basic regular blender. This blender embedds all messages into a mimemessage with subject &quot;VortexMessage&quot;, a user agent identifying a vortex message, and a body saying &quot;This is a VortexMessage&quot;.
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node COLOR="#338800" CREATED="1556184160426" ID="ID_1758086924" MODIFIED="1556185434135">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000">router =</font>
    </p>
  </body>
</html>
</richcontent>
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Name of the config section setting up the router for ths blending layer
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node CREATED="1556183100425" ID="ID_573024069" MODIFIED="1556184945573" POSITION="right">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p style="text-align: left">
      transport sections
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1556184412261" ID="ID_925555864" MODIFIED="1556184427515" TEXT="net.messagevortex.transport.SmtpImapServer"/>
<node CREATED="1556184430371" ID="ID_267714294" MODIFIED="1556184558122" TEXT="net.messagevortex.transport.MailServer">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Implementation reflecting a transport endpoint providing a pop3/Smtp protocol tuple for a MUA
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node CREATED="1556183180979" ID="ID_199750363" MODIFIED="1556184867141" POSITION="right">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p style="text-align: left">
      credential sources sections
    </p>
  </body>
</html></richcontent>
</node>
</node>
</map>
