<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1488379388387" ID="ID_1866334478" LINK="http://www.crypto.ethz.ch/teaching/lectures/KP17/" MODIFIED="1488437619216" TEXT="Cryptographic Protocols">
<node CREATED="1488436258281" ID="ID_1887060848" MODIFIED="1488976362582" POSITION="right" TEXT="Proof">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Pair of two interactive&#160;&#160;programs (P (proofer), V (Verifier)).
    </p>
    <p>
      
    </p>
    <p>
      V will only accept if P offers a valid series of answers.
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<node CREATED="1488790761906" ID="ID_1519496901" MODIFIED="1488790886743" TEXT="General">
<node CREATED="1488790771597" ID="ID_899635241" MODIFIED="1488790780363" TEXT="Must be concise"/>
<node CREATED="1488790781088" ID="ID_1920836669" MODIFIED="1488790793968" TEXT="Must be based on the given problem"/>
<node CREATED="1488790796153" ID="ID_1786821782" MODIFIED="1488790846245" TEXT="must proof that the rules to solve it have been followed"/>
</node>
<node CREATED="1488436334920" ID="ID_410048932" MODIFIED="1488436337207" TEXT="types">
<node CREATED="1488436262019" ID="ID_1363197896" MODIFIED="1488436702355" TEXT="static">
<node CREATED="1488436298205" ID="ID_1621735352" MODIFIED="1488436299460" TEXT="I just send my proof"/>
</node>
<node CREATED="1488436286397" ID="ID_732945148" MODIFIED="1488960998064" TEXT="interactive">
<node CREATED="1488436300784" ID="ID_564750979" MODIFIED="1488436328152" TEXT="Multiple steps forth and back are required"/>
<node CREATED="1488960998075" ID="ID_568886918" MODIFIED="1488961002359" TEXT="motivation">
<node CREATED="1488961003100" ID="ID_1397980384" MODIFIED="1488961034734" TEXT="Reduction of transfered information"/>
<node CREATED="1488961035193" ID="ID_1642444774" MODIFIED="1488961066028" TEXT="Variety of proofable statements"/>
</node>
<node CREATED="1488961080174" ID="ID_341485680" MODIFIED="1488961085620" TEXT="use cases ">
<node CREATED="1488961086864" ID="ID_1009897484" MODIFIED="1488961097458" TEXT="Base for digital signeage"/>
<node CREATED="1488961099560" ID="ID_1629318087" MODIFIED="1488961125285" TEXT="Identification protocol"/>
<node CREATED="1488961128324" ID="ID_1252701351" MODIFIED="1488961146615" TEXT="multi-party calculations"/>
</node>
<node CREATED="1488975754609" ID="ID_765645876" MODIFIED="1488975774291" TEXT="These proofs are in general more powerful"/>
</node>
</node>
<node CREATED="1488436350918" ID="ID_990598439" MODIFIED="1488436354091" TEXT="criteria">
<node CREATED="1488436354904" ID="ID_529236426" MODIFIED="1488961289221" TEXT="Completeness (Vollst&#xe4;ndigkeit)">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Wenn eine Aussage wahr ist (resp. im Fall eines Beweises von Wissen, wenn Peggy die fragliche Information kennt), dann wird der Beweis akzeptiert.
    </p>
    <p>
      
    </p>
    <p>
      Diese Anforderung kann auch abgeschw&#228;cht werden, indem man nur verlangt, dass der Beweis f&#252;r eine wahre Aussage mit
    </p>
    <p>
      &#252;berw&#228;ltigender (d.h. &#228;usserst nahe bei 1) Wahrscheinlichkeit akzeptiert wird. (Diese Abschw&#228;chung wird aber nicht n&#246;tig sein, d.h.
    </p>
    <p>
      in allen bekannten Beispielen ist die Wahrscheinlichkeit gleich 1.)
    </p>
  </body>
</html></richcontent>
<node CREATED="1488436360011" ID="ID_671804777" MODIFIED="1488436396901" TEXT="Accepts all cases where the prover knows the secret">
<node CREATED="1488436497229" ID="ID_922154334" MODIFIED="1488436508421" TEXT="usually 100% is required"/>
</node>
</node>
<node CREATED="1488436422459" ID="ID_1397835433" MODIFIED="1488961361826" TEXT="Soundness (Widerspruchsfreiheit)">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Wenn eine Aussage falsch ist (resp. Peggy die fragliche Information nicht kennt), dann gibt es keine Strategie f&#252;r Peggy, Vic zu &#252;berzeugen. Diese Anforderung kann auch abgeschw&#228;cht werden, indem man nur verlangt, dass jeder Beweis f&#252;r eine falsche Aussage mit h&#246;chstens vernachl&#228;ssigbarer, d.h. &#228;usserst kleiner Wahrscheinlichkeit akzeptiert wird.
    </p>
  </body>
</html></richcontent>
<node CREATED="1488436428870" ID="ID_719712473" MODIFIED="1488436471435" TEXT="Rejects all cases where the prover does not know the secret">
<node CREATED="1488436473152" ID="ID_1166868783" MODIFIED="1488436481358" TEXT="Must not be absolute"/>
<node CREATED="1488436481883" ID="ID_276272472" MODIFIED="1488436495096" TEXT="a little probability is acceptable"/>
</node>
</node>
<node CREATED="1488436511929" ID="ID_1017925759" MODIFIED="1488436644861" TEXT="(optional) zero-knowledge">
<node CREATED="1488436533173" ID="ID_711188075" MODIFIED="1490186405792" TEXT="trivial: Following the protocol does not reveal any information from the prover to the verifier"/>
<node CREATED="1490186408880" ID="ID_603195593" MODIFIED="1490186918397" TEXT="Formal">
<node CREATED="1490186422577" ID="ID_1572856481" MODIFIED="1490186473321" TEXT="Zero-Knowledge (ZK)">
<node CREATED="1490186477726" ID="ID_1989357067" MODIFIED="1490186501559" TEXT="Transscript and simulated Transscript are indistinguishable"/>
<node CREATED="1490186504412" ID="ID_1840419716" MODIFIED="1490186537523" TEXT="Runtime is polynomially bounded"/>
</node>
<node CREATED="1490186541225" ID="ID_1262142804" MODIFIED="1490187039217" TEXT="Black-Box-Zero-Knowledge (BB-ZK)">
<node CREATED="1490186648035" ID="ID_158878207" MODIFIED="1490186896537" TEXT="Transscript of an possibly cheating is indstinguishable "/>
<node CREATED="1490186921854" ID="ID_190636856" MODIFIED="1490186924148" TEXT="Runtime is polynomially bounded "/>
</node>
<node CREATED="1490186941953" ID="ID_1737898532" MODIFIED="1490187074913" TEXT="Honest-verifyer zero-knowledge (HVZK)">
<icon BUILTIN="yes"/>
</node>
</node>
<node CREATED="1490187813808" ID="ID_1477914116" MODIFIED="1490187845245" TEXT="types">
<icon BUILTIN="down"/>
<node CREATED="1490187819099" ID="ID_596154690" MODIFIED="1490187822737" TEXT="perfect"/>
<node CREATED="1490187823182" ID="ID_1641242076" MODIFIED="1490187831260" TEXT="statistical"/>
<node CREATED="1490187832040" ID="ID_1011087498" MODIFIED="1490187838823" TEXT="computational"/>
</node>
</node>
<node CREATED="1488963069744" ID="ID_1992504849" MODIFIED="1488963080627" TEXT="additional">
<node CREATED="1488436646496" ID="ID_1480636869" MODIFIED="1488961583510" TEXT="efficient">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      - den Berechnungsaufwand f&#252;r Peggy
    </p>
    <p>
      - den Berechnungsaufwand f&#252;r Vic
    </p>
    <p>
      - den Kommunikationsaufwand
    </p>
    <p>
      - die Anzahl Runden eines Protokolls.
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1488961415060" ID="ID_954910646" MODIFIED="1488963083971" TEXT="generality">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Welche Typen von Aussagen k&#246;nnen mit dem Verfahren bewiesen werden? Gewisse Verfahren sind sehr allgemein und erlauben jede m&#246;gliche Aussage zu beweisen. Andere Verfahren sind auf Aussagen eines ganz bestimmten Typs beschr&#228;nkt (z.B. ob ein Graph einen Hamiltonschen Kreis besitzt).
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1488961421066" ID="ID_1669718203" MODIFIED="1488961505586" TEXT="Information loss">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Wieviel Information erh&#228;lt der Verifizierer durch das Protokoll? (Z.B. keine Information, oder zumindest keine n&#252;tzliche Information.)
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1488961433226" ID="ID_645551847" MODIFIED="1488963476845" TEXT="type of security/cryptographic assumptions">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Welche kryptographischen Annahmen werden gemacht? (Z.B. Schwierigkeit des Faktorisierens grosser Zahlen.) Die Sicherheit kann f&#252;r Peggy (oder Vic) informationstheoretisch sein, d.h. die andere Partei kann selbst mit unendlichen Computerressourcen nicht betr&#252;gen resp. Information erhalten.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node CREATED="1488436704673" ID="ID_1948846456" MODIFIED="1488436707909" TEXT="category">
<node CREATED="1488436709680" ID="ID_619317623" MODIFIED="1488436714102" TEXT="Non-Formal">
<node CREATED="1488436719620" ID="ID_516242814" MODIFIED="1488436729632" TEXT="statement"/>
<node CREATED="1488436730565" ID="ID_1915778434" MODIFIED="1488436732833" TEXT="proof">
<node CREATED="1488436741004" ID="ID_213879718" MODIFIED="1488436744985" TEXT="The result"/>
</node>
<node CREATED="1488436733340" ID="ID_763680861" MODIFIED="1488436737585" TEXT="verification">
<node CREATED="1488436747886" ID="ID_1343081943" MODIFIED="1488436777195" TEXT="Steps to verify that the result reflects a valid solution to the statement"/>
</node>
</node>
<node CREATED="1488436714609" ID="ID_620243485" MODIFIED="1488436717159" TEXT="Formal">
<node CREATED="1488436782616" ID="ID_340750859" MODIFIED="1488436789747" TEXT="Class of statements"/>
<node CREATED="1488436796095" ID="ID_286029" MODIFIED="1488436815941" TEXT="derived statement (eg. a specific case of the class)"/>
<node CREATED="1488436817945" ID="ID_1450170763" MODIFIED="1488436825233" TEXT="Proof"/>
<node CREATED="1488436826490" ID="ID_568447487" MODIFIED="1488436832814" TEXT="Verification"/>
</node>
</node>
<node CREATED="1488436987048" ID="ID_663283685" MODIFIED="1488790869833" TEXT="Content">
<node CREATED="1488437074172" ID="ID_1447081700" MODIFIED="1488437080560" TEXT="Proof of statement">
<node CREATED="1488437094413" ID="ID_20102241" MODIFIED="1488437115290" TEXT="Sudoku  has a solution (which I possibly do not know)"/>
</node>
<node CREATED="1488437081098" ID="ID_1389719460" MODIFIED="1488963115185" TEXT="Proof of knowledge">
<node CREATED="1488437118286" ID="ID_1983044383" MODIFIED="1488437131498" TEXT="I know a solution to this Sudoku"/>
</node>
</node>
<node CREATED="1488437202513" ID="ID_734011778" MODIFIED="1488437207534" TEXT="exercices">
<node CREATED="1488437208146" ID="ID_755298665" MODIFIED="1488437211526" TEXT="Padlocks">
<node CREATED="1488437212200" ID="ID_780179537" MODIFIED="1488437222662" TEXT="knows one out of two">
<node CREATED="1488974624894" ID="ID_1321493281" MODIFIED="1488974636992" TEXT="Provided solution">
<node CREATED="1488786642779" ID="ID_621833091" MODIFIED="1488786651446" TEXT="daisychain padlocks"/>
</node>
<node CREATED="1488974644060" ID="ID_1773644716" MODIFIED="1488974653254" TEXT="Own solutions">
<node CREATED="1488380101513" ID="ID_1469895401" MODIFIED="1488380249961" TEXT="Solution 2">
<node CREATED="1488380251068" ID="ID_1529980453" MODIFIED="1488380262958" TEXT="We have a chemistry box"/>
<node CREATED="1488380265454" ID="ID_76540704" MODIFIED="1488380277903" TEXT="Both locks are in the box"/>
<node CREATED="1488380307842" ID="ID_278957836" MODIFIED="1488974528464" TEXT="Vic cannot see into the box (eppgy can)"/>
<node CREATED="1488380278610" ID="ID_562379298" MODIFIED="1488380298072" TEXT="Peggy may lock a ring with one of the padlocks"/>
</node>
<node CREATED="1488379439823" ID="ID_1205867101" MODIFIED="1488379443816" TEXT="Solution 1">
<node CREATED="1488379424554" ID="ID_329595889" MODIFIED="1488379457930" TEXT="Cover Padlock wheels with Permutation">
<node CREATED="1488379458852" ID="ID_1125981976" MODIFIED="1488379468693" TEXT="each individually"/>
</node>
<node CREATED="1488379470925" ID="ID_1145703489" MODIFIED="1488379493848" TEXT="Tell valid permutation"/>
<node CREATED="1488379498548" ID="ID_1131799201" MODIFIED="1488379501772" TEXT="downsides">
<node CREATED="1488974550215" ID="ID_782695440" MODIFIED="1488974556944" TEXT="not zero knowledge">
<node CREATED="1488379502797" ID="ID_401684551" MODIFIED="1488379510277" TEXT="Lock is known afterwards"/>
</node>
<node CREATED="1488974573964" ID="ID_1770070776" MODIFIED="1488974594094" TEXT="Peggy might just be lucky (not sound)"/>
</node>
</node>
</node>
</node>
<node CREATED="1488437224001" ID="ID_274729583" MODIFIED="1488437235654" TEXT="knows one out of 100">
<node CREATED="1488786756010" ID="ID_910730880" MODIFIED="1488786762253" TEXT="or ">
<node CREATED="1488786762928" ID="ID_1065903240" MODIFIED="1488786772404" TEXT="interconnect two rings"/>
<node CREATED="1488786657960" ID="ID_862535780" MODIFIED="1488786676939" TEXT="make a ring to a knot"/>
</node>
</node>
<node CREATED="1488437237347" ID="ID_1078824766" MODIFIED="1488786886780" TEXT="knows 2 of 7">
<node CREATED="1488786681189" ID="ID_1667461976" MODIFIED="1488786754553" TEXT="make two rings">
<node CREATED="1488786809361" ID="ID_1003358917" MODIFIED="1488786817076" TEXT="not zero-knowledge"/>
</node>
<node CREATED="1488786826695" ID="ID_930876238" MODIFIED="1488786869636" TEXT="proof that she can open at least one padlock out of 6">
<node CREATED="1488786898595" ID="ID_342893110" MODIFIED="1488786900959" TEXT="see above"/>
</node>
</node>
</node>
<node CREATED="1488437253123" ID="ID_816253832" MODIFIED="1488437256184" TEXT="Kitkat">
<node CREATED="1488437256676" ID="ID_934966064" MODIFIED="1488437391509" TEXT="One out of two is distinguishable"/>
<node CREATED="1488437392924" ID="ID_1751481134" MODIFIED="1488437405910" TEXT="Three Kitkats are distinguishable"/>
<node CREATED="1488437418385" ID="ID_794367252" MODIFIED="1488437430759" TEXT="One out of three is distinguishable"/>
</node>
<node CREATED="1488437437755" ID="ID_1750632124" MODIFIED="1488786445121" TEXT="Waldo">
<node CREATED="1488437448490" ID="ID_1596093814" MODIFIED="1488786457274" TEXT="Know where waldo is">
<node CREATED="1488786458147" ID="ID_1080670560" MODIFIED="1488786511522" TEXT="cardboard solution">
<node CREATED="1488786512027" ID="ID_578306568" MODIFIED="1488786528440" TEXT="Cardboard twice the size with a hole"/>
<node CREATED="1488786551386" ID="ID_1288931314" MODIFIED="1488786568848" TEXT="blindly show waldos location"/>
<node CREATED="1488786595173" ID="ID_585033946" MODIFIED="1488786625907" TEXT="Proof later that correct paper has been used by covering the hole and getting the paper"/>
<node CREATED="1488786570600" ID="ID_1001391285" MODIFIED="1488786589376" TEXT="not zero knowledge: Leaks information about Waldos surrounding"/>
</node>
</node>
</node>
</node>
</node>
<node CREATED="1488979773547" ID="ID_917478507" MODIFIED="1488979803690" POSITION="right" TEXT="Proof examples">
<node CREATED="1488979792363" ID="ID_967965622" MODIFIED="1488979796385" TEXT="Graphs">
<node CREATED="1488979693479" ID="ID_223798045" MODIFIED="1488979702576" TEXT="Isomorphismus beweisen">
<node CREATED="1488976391546" ID="ID_1351035893" MODIFIED="1488977713939" TEXT="Es muss eine Permutationsmatrix geben">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Matrix, die eine 1 in jeder Zeile und Spalte enth&#228;lt (Rest ist 0).
    </p>
    <p>
      
    </p>
    <p>
      Statement: G_1=\sigma G_0 \sigma^-1
    </p>
    <p>
      Verfikation: Eine&#160;&#160;zuf&#228;llige Matrix T=\pi G_0 \pi^-1 wird eingef&#252;hrt. Peggy muss beweisen, dass entweder T eine Permutation von G_0 ist (Challenge 1: \rho=\pi) oder eine Permutation von G_1 (Challenge 2:\rho=\pi \sigma^-1) ist.
    </p>
    <p>
      
    </p>
    <p>
      Jede Permutation \pi darf nur einmal verwendet werden. Sonst k&#246;nnen beide Challenges erhalten werden und weil \pi/(\pi \sigma^-1)=\sigma gilt kann die Antwort berechnet werden.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node CREATED="1488979710398" ID="ID_329809293" MODIFIED="1488979725736" TEXT="non-isomorphism beweisen">
<node CREATED="1488979727566" ID="ID_131532266" MODIFIED="1488979761991" TEXT="is only non-zero-knowledge to dishonest Verifier"/>
</node>
</node>
<node CREATED="1488979865602" ID="ID_11985081" MODIFIED="1488979876496" TEXT="RSA">
<node CREATED="1488979879512" ID="ID_1403979974" MODIFIED="1488980059074" TEXT="Fiat-Shamir"/>
</node>
</node>
<node CREATED="1488980456686" ID="ID_630865186" MODIFIED="1488981435304" POSITION="left" TEXT="Complexity Theory">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Anhang 1.B
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="stop-sign"/>
<node CREATED="1488980597275" ID="ID_658015431" MODIFIED="1488980601287" TEXT="Language">
<node CREATED="1488980602296" ID="ID_1778479154" MODIFIED="1488981345834" TEXT="Set of words">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Listet einfach alle guten Graphen auf. Der Prover muss nur beweisen, dass eine L&#246;sung in der Sprache enthalten ist.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node CREATED="1488981451727" ID="ID_1787689799" MODIFIED="1488981515520" TEXT="Algorithm is efficient if Runningtime is bounded f(|x|) must be polinomial."/>
<node CREATED="1488981556760" ID="ID_1271290278" MODIFIED="1488981683953" TEXT="f is neglible">
<node CREATED="1488981566469" ID="ID_644624235" MODIFIED="1489037976374" TEXT="Must decrease faster than 1/n^c">
<icon BUILTIN="stop-sign"/>
</node>
<node CREATED="1489037891096" ID="ID_1156433962" MODIFIED="1489037938742" TEXT="Example: 2^{-n}"/>
<node CREATED="1489581371931" ID="ID_1061005951" MODIFIED="1489581430277" TEXT="If f is neglible (or poly x neglible) probability is good enough for us">
<icon BUILTIN="yes"/>
</node>
</node>
<node CREATED="1488981647200" ID="ID_606882033" MODIFIED="1488981766766" TEXT="noticeable">
<node CREATED="1489037912952" ID="ID_1421091380" MODIFIED="1489037932551" TEXT="Example: n^-2"/>
</node>
</node>
<node CREATED="1489578979891" ID="ID_975973078" MODIFIED="1489578995045" POSITION="left" TEXT="Fiat-Shamir protocol">
<node CREATED="1489640453845" ID="ID_509045801" MODIFIED="1489640487459" TEXT="fiat-shamir heuristic">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Vermutlich Pr&#252;fungsrelevant
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="messagebox_warning"/>
</node>
<node CREATED="1490186600177" ID="ID_1697726801" MODIFIED="1490186607274" TEXT="Is zero-knowledge"/>
</node>
<node CREATED="1489581472509" ID="ID_697322789" MODIFIED="1489581479248" POSITION="right" TEXT="Fragen">
<node CREATED="1489581481924" ID="ID_1803130025" MODIFIED="1489581488623" TEXT="NP">
<node CREATED="1489581494406" ID="ID_758504994" MODIFIED="1489582199638" TEXT="Es muss eine Sprache geben, welche durch eine non-det TM akkzeptiert wird"/>
</node>
</node>
</node>
</map>
