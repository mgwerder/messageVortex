var stopWords     = new Array( 'REMOVE','ME','NO','ANTI','SPAM','KIDDING','ENTFERNE','MICH' )
var replaceWords  = new Array(
                       new Array("at"  ,"@"),
                       new Array("dot" ,"."),
                       new Array("dash","-")
                    )

function private_remover(str) {
  str= " "+str.replace(new RegExp("_","gi")," ")+" "
  for(var i=0;i<stopWords.length;i++) {
    var re = new RegExp(" "+stopWords[i]+" ", 'gi')
    str = str.replace(re, ' ')
    var re = new RegExp("%20"+stopWords[i]+"%20", 'gi')
    str = str.replace(re, '%20')
  }
  return str.substr(1,str.length-2)
}

function private_replacer(str) {
  str= " "+str.replace(new RegExp("_","gi")," ")+" "
  for(var i=0;i<replaceWords.length;i++) {
    var re = new RegExp(" "+replaceWords[i][0]+" ", 'gi')
    str = str.replace(re, replaceWords[i][1] )
    var re = new RegExp("%20"+replaceWords[i][0]+"%20", 'gi')
    str = str.replace(re, replaceWords[i][1] )
  }
  return str.substr(1,str.length-2)
}

function emailDecypher(obj) {
  if(obj==null) {
    obj=document.getElementById('email1')
    for(var i=1;obj != null;i++) {
      emailDecypher(obj)
      obj=document.getElementById('email'+(i+1))
    }
  } else {
    obj.href     = private_replacer( private_remover(obj.href)      )
    obj.innerHTML= private_replacer( private_remover(obj.innerHTML) )
  }
}

emailDecypher(null)