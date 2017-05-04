<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1488379388387" ID="ID_1866334478" LINK="http://www.crypto.ethz.ch/teaching/lectures/KP17/" MODIFIED="1491391300860" STYLE="fork" TEXT="Cryptographic Protocols">
<node CREATED="1488436258281" ID="ID_1887060848" MODIFIED="1491391300104" POSITION="right" TEXT="Proof">
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
<node CREATED="1488790761906" ID="ID_1519496901" MODIFIED="1491391300104" TEXT="General">
<node CREATED="1488790771597" ID="ID_899635241" MODIFIED="1491391300104" TEXT="Must be concise"/>
<node CREATED="1488790781088" ID="ID_1920836669" MODIFIED="1491391300104" TEXT="Must be based on the given problem"/>
<node CREATED="1488790796153" ID="ID_1786821782" MODIFIED="1491391300105" TEXT="must proof that the rules to solve it have been followed"/>
</node>
<node CREATED="1488436334920" ID="ID_410048932" MODIFIED="1491391300105" TEXT="types">
<node CREATED="1488436262019" ID="ID_1363197896" MODIFIED="1491391300105" TEXT="static">
<node CREATED="1488436298205" ID="ID_1621735352" MODIFIED="1491391300105" TEXT="I just send my proof"/>
</node>
<node CREATED="1488436286397" ID="ID_732945148" MODIFIED="1491391300105" TEXT="interactive">
<node CREATED="1488436300784" ID="ID_564750979" MODIFIED="1491391300105" TEXT="Multiple steps forth and back are required"/>
<node CREATED="1488960998075" ID="ID_568886918" MODIFIED="1491391300106" TEXT="motivation">
<node CREATED="1488961003100" ID="ID_1397980384" MODIFIED="1491391300106" TEXT="Reduction of transfered information"/>
<node CREATED="1488961035193" ID="ID_1642444774" MODIFIED="1491391300106" TEXT="Variety of proofable statements"/>
</node>
<node CREATED="1488961080174" ID="ID_341485680" MODIFIED="1491391300106" TEXT="use cases ">
<node CREATED="1488961086864" ID="ID_1009897484" MODIFIED="1491391300106" TEXT="Base for digital signeage"/>
<node CREATED="1488961099560" ID="ID_1629318087" MODIFIED="1491391300106" TEXT="Identification protocol"/>
<node CREATED="1488961128324" ID="ID_1252701351" MODIFIED="1491391300107" TEXT="multi-party calculations"/>
</node>
<node CREATED="1488975754609" ID="ID_765645876" MODIFIED="1491391300107" TEXT="These proofs are in general more powerful"/>
</node>
</node>
<node CREATED="1488436350918" ID="ID_990598439" MODIFIED="1491391300107" TEXT="criteria">
<node CREATED="1488436354904" ID="ID_529236426" MODIFIED="1491391300107" TEXT="Completeness (Vollst&#xe4;ndigkeit)">
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
<node CREATED="1488436360011" ID="ID_671804777" MODIFIED="1491391300107" TEXT="Accepts all cases where the prover knows the secret">
<node CREATED="1488436497229" ID="ID_922154334" MODIFIED="1491391300108" TEXT="usually 100% is required"/>
</node>
</node>
<node CREATED="1488436422459" ID="ID_1397835433" MODIFIED="1491391300108" TEXT="Soundness (Widerspruchsfreiheit)">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Wenn eine Aussage falsch ist (resp. Peggy die fragliche Information nicht kennt), dann gibt es keine Strategie f&#252;r Peggy, Vic zu &#252;berzeugen. Diese Anforderung kann auch abgeschw&#228;cht werden, indem man nur verlangt, dass jeder Beweis f&#252;r eine falsche Aussage mit h&#246;chstens vernachl&#228;ssigbarer, d.h. &#228;usserst kleiner Wahrscheinlichkeit akzeptiert wird.
    </p>
  </body>
</html></richcontent>
<node CREATED="1488436428870" ID="ID_719712473" MODIFIED="1491391300108" TEXT="Rejects all cases where the prover does not know the secret">
<node CREATED="1488436473152" ID="ID_1166868783" MODIFIED="1491391300108" TEXT="Must not be absolute"/>
<node CREATED="1488436481883" ID="ID_276272472" MODIFIED="1491391300108" TEXT="a little probability is acceptable"/>
</node>
</node>
<node CREATED="1488436511929" ID="ID_1017925759" MODIFIED="1491391300109" TEXT="(optional) zero-knowledge">
<node CREATED="1488436533173" ID="ID_711188075" MODIFIED="1491391300109" TEXT="trivial: Following the protocol does not reveal any information from the prover to the verifier"/>
<node CREATED="1490186408880" ID="ID_603195593" MODIFIED="1491391300109" TEXT="Formal">
<node CREATED="1490186422577" ID="ID_1572856481" MODIFIED="1491391300109" TEXT="Zero-Knowledge (ZK)">
<node CREATED="1490186477726" ID="ID_1989357067" MODIFIED="1491391300110" TEXT="Transscript and simulated Transscript are indistinguishable"/>
<node CREATED="1490186504412" ID="ID_1840419716" MODIFIED="1491391300111" TEXT="Runtime is polynomially bounded"/>
</node>
<node CREATED="1490186541225" ID="ID_1262142804" MODIFIED="1491391300111" TEXT="Black-Box-Zero-Knowledge (BB-ZK)">
<node CREATED="1490186648035" ID="ID_158878207" MODIFIED="1491391300111" TEXT="Transscript of an possibly cheating is indstinguishable "/>
<node CREATED="1490186921854" ID="ID_190636856" MODIFIED="1491391300111" TEXT="Runtime is polynomially bounded "/>
</node>
<node CREATED="1490186941953" ID="ID_1737898532" MODIFIED="1491391300111" TEXT="Honest-verifyer zero-knowledge (HVZK)">
<icon BUILTIN="yes"/>
</node>
</node>
<node CREATED="1490187813808" ID="ID_1477914116" MODIFIED="1491391300112" TEXT="types">
<icon BUILTIN="down"/>
<node CREATED="1490187819099" ID="ID_596154690" MODIFIED="1491391300112" TEXT="perfect"/>
<node CREATED="1490187823182" ID="ID_1641242076" MODIFIED="1491391300113" TEXT="statistical"/>
<node CREATED="1490187832040" ID="ID_1011087498" MODIFIED="1491391300113" TEXT="computational"/>
</node>
</node>
<node CREATED="1488963069744" ID="ID_1992504849" MODIFIED="1491391300113" TEXT="additional">
<node CREATED="1488436646496" ID="ID_1480636869" MODIFIED="1491391300113" TEXT="efficient">
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
<node CREATED="1488961415060" ID="ID_954910646" MODIFIED="1491391300114" TEXT="generality">
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
<node CREATED="1488961421066" ID="ID_1669718203" MODIFIED="1491391300114" TEXT="Information loss">
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
<node CREATED="1488961433226" ID="ID_645551847" MODIFIED="1491391300114" TEXT="type of security/cryptographic assumptions">
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
<node CREATED="1488436704673" ID="ID_1948846456" MODIFIED="1491391300115" TEXT="category">
<node CREATED="1488436709680" ID="ID_619317623" MODIFIED="1491391300115" TEXT="Non-Formal">
<node CREATED="1488436719620" ID="ID_516242814" MODIFIED="1491391300115" TEXT="statement"/>
<node CREATED="1488436730565" ID="ID_1915778434" MODIFIED="1491391300115" TEXT="proof">
<node CREATED="1488436741004" ID="ID_213879718" MODIFIED="1491391300115" TEXT="The result"/>
</node>
<node CREATED="1488436733340" ID="ID_763680861" MODIFIED="1491391300115" TEXT="verification">
<node CREATED="1488436747886" ID="ID_1343081943" MODIFIED="1491391300115" TEXT="Steps to verify that the result reflects a valid solution to the statement"/>
</node>
</node>
<node CREATED="1488436714609" ID="ID_620243485" MODIFIED="1491391300116" TEXT="Formal">
<node CREATED="1488436782616" ID="ID_340750859" MODIFIED="1491391300116" TEXT="Class of statements"/>
<node CREATED="1488436796095" ID="ID_286029" MODIFIED="1491391300116" TEXT="derived statement (eg. a specific case of the class)"/>
<node CREATED="1488436817945" ID="ID_1450170763" MODIFIED="1491391300116" TEXT="Proof"/>
<node CREATED="1488436826490" ID="ID_568447487" MODIFIED="1491391300117" TEXT="Verification"/>
</node>
</node>
<node CREATED="1488436987048" ID="ID_663283685" MODIFIED="1491391300117" TEXT="Content">
<node CREATED="1488437074172" ID="ID_1447081700" MODIFIED="1491391300117" TEXT="Proof of statement">
<node CREATED="1488437094413" ID="ID_20102241" MODIFIED="1491391300117" TEXT="Sudoku  has a solution (which I possibly do not know)"/>
</node>
<node CREATED="1488437081098" ID="ID_1389719460" MODIFIED="1491391300117" TEXT="Proof of knowledge">
<node CREATED="1488437118286" ID="ID_1983044383" MODIFIED="1491391300117" TEXT="I know a solution to this Sudoku"/>
</node>
</node>
<node CREATED="1488437202513" ID="ID_734011778" MODIFIED="1491391300118" TEXT="exercices">
<node CREATED="1488437208146" ID="ID_755298665" MODIFIED="1491391300118" TEXT="Padlocks">
<node CREATED="1488437212200" ID="ID_780179537" MODIFIED="1491391300118" TEXT="knows one out of two">
<node CREATED="1488974624894" ID="ID_1321493281" MODIFIED="1491391300118" TEXT="Provided solution">
<node CREATED="1488786642779" ID="ID_621833091" MODIFIED="1491391300118" TEXT="daisychain padlocks"/>
</node>
<node CREATED="1488974644060" ID="ID_1773644716" MODIFIED="1491391300118" TEXT="Own solutions">
<node CREATED="1488380101513" ID="ID_1469895401" MODIFIED="1491391300118" TEXT="Solution 2">
<node CREATED="1488380251068" ID="ID_1529980453" MODIFIED="1491391300119" TEXT="We have a chemistry box"/>
<node CREATED="1488380265454" ID="ID_76540704" MODIFIED="1491391300119" TEXT="Both locks are in the box"/>
<node CREATED="1488380307842" ID="ID_278957836" MODIFIED="1491391300119" TEXT="Vic cannot see into the box (eppgy can)"/>
<node CREATED="1488380278610" ID="ID_562379298" MODIFIED="1491391300119" TEXT="Peggy may lock a ring with one of the padlocks"/>
</node>
<node CREATED="1488379439823" ID="ID_1205867101" MODIFIED="1491391300119" TEXT="Solution 1">
<node CREATED="1488379424554" ID="ID_329595889" MODIFIED="1491391300119" TEXT="Cover Padlock wheels with Permutation">
<node CREATED="1488379458852" ID="ID_1125981976" MODIFIED="1491391300120" TEXT="each individually"/>
</node>
<node CREATED="1488379470925" ID="ID_1145703489" MODIFIED="1491391300120" TEXT="Tell valid permutation"/>
<node CREATED="1488379498548" ID="ID_1131799201" MODIFIED="1491391300120" TEXT="downsides">
<node CREATED="1488974550215" ID="ID_782695440" MODIFIED="1491391300120" TEXT="not zero knowledge">
<node CREATED="1488379502797" ID="ID_401684551" MODIFIED="1491391300120" TEXT="Lock is known afterwards"/>
</node>
<node CREATED="1488974573964" ID="ID_1770070776" MODIFIED="1491391300120" TEXT="Peggy might just be lucky (not sound)"/>
</node>
</node>
</node>
</node>
<node CREATED="1488437224001" ID="ID_274729583" MODIFIED="1491391300121" TEXT="knows one out of 100">
<node CREATED="1488786756010" ID="ID_910730880" MODIFIED="1491391300121" TEXT="or ">
<node CREATED="1488786762928" ID="ID_1065903240" MODIFIED="1491391300121" TEXT="interconnect two rings"/>
<node CREATED="1488786657960" ID="ID_862535780" MODIFIED="1491391300121" TEXT="make a ring to a knot"/>
</node>
</node>
<node CREATED="1488437237347" ID="ID_1078824766" MODIFIED="1491391300121" TEXT="knows 2 of 7">
<node CREATED="1488786681189" ID="ID_1667461976" MODIFIED="1491391300121" TEXT="make two rings">
<node CREATED="1488786809361" ID="ID_1003358917" MODIFIED="1491391300121" TEXT="not zero-knowledge"/>
</node>
<node CREATED="1488786826695" ID="ID_930876238" MODIFIED="1491391300122" TEXT="proof that she can open at least one padlock out of 6">
<node CREATED="1488786898595" ID="ID_342893110" MODIFIED="1491391300122" TEXT="see above"/>
</node>
</node>
</node>
<node CREATED="1488437253123" ID="ID_816253832" MODIFIED="1491391300123" TEXT="Kitkat">
<node CREATED="1488437256676" ID="ID_934966064" MODIFIED="1491391300123" TEXT="One out of two is distinguishable"/>
<node CREATED="1488437392924" ID="ID_1751481134" MODIFIED="1491391300123" TEXT="Three Kitkats are distinguishable"/>
<node CREATED="1488437418385" ID="ID_794367252" MODIFIED="1491391300123" TEXT="One out of three is distinguishable"/>
</node>
<node CREATED="1488437437755" ID="ID_1750632124" MODIFIED="1491391300123" TEXT="Waldo">
<node CREATED="1488437448490" ID="ID_1596093814" MODIFIED="1491391300124" TEXT="Know where waldo is">
<node CREATED="1488786458147" ID="ID_1080670560" MODIFIED="1491391300124" TEXT="cardboard solution">
<node CREATED="1488786512027" ID="ID_578306568" MODIFIED="1491391300124" TEXT="Cardboard twice the size with a hole"/>
<node CREATED="1488786551386" ID="ID_1288931314" MODIFIED="1491391300124" TEXT="blindly show waldos location"/>
<node CREATED="1488786595173" ID="ID_585033946" MODIFIED="1491391300124" TEXT="Proof later that correct paper has been used by covering the hole and getting the paper"/>
<node CREATED="1488786570600" ID="ID_1001391285" MODIFIED="1491391300124" TEXT="not zero knowledge: Leaks information about Waldos surrounding"/>
</node>
</node>
</node>
</node>
</node>
<node CREATED="1488979773547" ID="ID_917478507" MODIFIED="1491391300125" POSITION="right" TEXT="Proof examples">
<node CREATED="1488979792363" ID="ID_967965622" MODIFIED="1491391300125" TEXT="Graphs">
<node CREATED="1488979693479" ID="ID_223798045" MODIFIED="1491391300125" TEXT="Isomorphismus beweisen">
<node CREATED="1488976391546" ID="ID_1351035893" MODIFIED="1491391300125" TEXT="Es muss eine Permutationsmatrix geben">
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
<node CREATED="1488979710398" ID="ID_329809293" MODIFIED="1491391300125" TEXT="non-isomorphism beweisen">
<node CREATED="1488979727566" ID="ID_131532266" MODIFIED="1491391300126" TEXT="is only non-zero-knowledge to dishonest Verifier"/>
</node>
</node>
<node CREATED="1488979865602" ID="ID_11985081" MODIFIED="1491391300126" TEXT="RSA">
<node CREATED="1488979879512" ID="ID_1403979974" MODIFIED="1491391300126" TEXT="Fiat-Shamir"/>
</node>
</node>
<node CREATED="1488980456686" ID="ID_630865186" MODIFIED="1491391300126" POSITION="left" TEXT="Complexity Theory">
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
<node CREATED="1488980597275" ID="ID_658015431" MODIFIED="1491391300126" TEXT="Language">
<node CREATED="1488980602296" ID="ID_1778479154" MODIFIED="1491391300127" TEXT="Set of words">
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
<node CREATED="1488981451727" ID="ID_1787689799" MODIFIED="1491391300127" TEXT="Algorithm is efficient if Runningtime is bounded f(|x|) must be polinomial."/>
<node CREATED="1488981556760" ID="ID_1271290278" MODIFIED="1491391300127" TEXT="f is neglible">
<node CREATED="1488981566469" ID="ID_644624235" MODIFIED="1491391300127" TEXT="Must decrease faster than 1/n^c">
<icon BUILTIN="stop-sign"/>
</node>
<node CREATED="1489037891096" ID="ID_1156433962" MODIFIED="1491391300127" TEXT="Example: 2^{-n}"/>
<node CREATED="1489581371931" ID="ID_1061005951" MODIFIED="1491391300127" TEXT="If f is neglible (or poly x neglible) probability is good enough for us">
<icon BUILTIN="yes"/>
</node>
</node>
<node CREATED="1488981647200" ID="ID_606882033" MODIFIED="1491391300128" TEXT="noticeable">
<node CREATED="1489037912952" ID="ID_1421091380" MODIFIED="1491391300128" TEXT="Example: n^-2"/>
</node>
</node>
<node CREATED="1489578979891" ID="ID_975973078" MODIFIED="1491391300128" POSITION="left" TEXT="Fiat-Shamir protocol"/>
<node CREATED="1489581472509" ID="ID_697322789" MODIFIED="1491391300130" POSITION="right" TEXT="Fragen">
<node CREATED="1489581481924" ID="ID_1803130025" MODIFIED="1491391300130" TEXT="NP">
<node CREATED="1489581494406" ID="ID_758504994" MODIFIED="1491391300130" TEXT="Es muss eine Sprache geben, welche durch eine non-det TM akkzeptiert wird"/>
</node>
</node>
<node CREATED="1491391321569" ID="ID_1404430249" MODIFIED="1491391355226" POSITION="left" TEXT="Protocol">
<node CREATED="1491391356153" ID="ID_1142924817" MODIFIED="1491391360188" TEXT="Fiat-Shamir">
<node CREATED="1489640453845" ID="ID_509045801" MODIFIED="1491391300128" TEXT="fiat-shamir heuristic">
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
<node CREATED="1490186600177" ID="ID_1697726801" MODIFIED="1491391300129" TEXT="Is zero-knowledge"/>
</node>
<node CREATED="1491391360730" ID="ID_1110462786" MODIFIED="1491391401720" TEXT="Guillou-Quisuatter">
<node CREATED="1491395026915" ID="ID_579725932" MODIFIED="1491395041498" TEXT="important! : e requires to be a prime"/>
</node>
<node CREATED="1491391411794" ID="ID_1016735980" MODIFIED="1491391414438" TEXT="Schnorr"/>
<node CREATED="1491391417059" ID="ID_607116507" LINK="Maurer09.pdf" MODIFIED="1491395759060" TEXT="Unified">
<node CREATED="1491391422954" ID="ID_1265478183" MODIFIED="1491391632756" TEXT="Peggy knows x elem G; Vic knows Z elem H">
<node CREATED="1491391443952" ID="ID_1906272611" MODIFIED="1491391487648" TEXT="fiat-shamir: G=H=Z^*_m"/>
<node CREATED="1491391496819" ID="ID_349075263" MODIFIED="1491391521026" TEXT="Guillou-Qusquater: G=H=Z^*_m "/>
<node CREATED="1491391522818" ID="ID_1634105546" MODIFIED="1491391606729" TEXT="Schnorr: G=Z_q; H s.t. |H&#xa6;=q"/>
</node>
<node CREATED="1491391696749" ID="ID_1928435267" MODIFIED="1491391701916" TEXT="c">
<node CREATED="1491391705236" ID="ID_1425609146" MODIFIED="1491391728011" TEXT="Fiat-Shamir: c={0,1}"/>
<node CREATED="1491391730427" ID="ID_404059252" MODIFIED="1491391940457" TEXT="Guillou-Quisquatter:C={0, ... ,e-1}"/>
<node CREATED="1491391941506" ID="ID_1980557728" MODIFIED="1491391976575" TEXT="Schnorr:c={0, ... , q-1}"/>
</node>
<node CREATED="1491391988585" ID="ID_812219308" MODIFIED="1491392241003" TEXT="k elem G; t=f(k)"/>
<node CREATED="1491392139887" ID="ID_101677629" MODIFIED="1491392223296" TEXT="F(x)"/>
<node CREATED="1491392304031" ID="ID_1796404915" MODIFIED="1491392308265" TEXT="completness"/>
<node CREATED="1491392310466" ID="ID_1342750058" MODIFIED="1491392313876" TEXT="soundness"/>
<node CREATED="1491392655542" ID="ID_1418693417" MODIFIED="1491392678100" TEXT="zero-knowlwdge">
<node CREATED="1491392811441" ID="ID_1213839593" MODIFIED="1491392820961" TEXT="c-simulateable"/>
<node CREATED="1491392825588" ID="ID_1205101869" MODIFIED="1491392853870" TEXT="Only if poly bounded challenge space"/>
</node>
<node CREATED="1491392906671" ID="ID_1745942109" MODIFIED="1491392913436" TEXT="proof of knowledge">
<node CREATED="1491392916873" ID="ID_1361423318" MODIFIED="1491392927978" TEXT="2-extractable"/>
<node CREATED="1491392967137" ID="ID_726600221" MODIFIED="1491392997450" TEXT="only if a good witness is available"/>
</node>
<node CREATED="1491392709102" ID="ID_955243207" MODIFIED="1491392727351" TEXT="Important conditions">
<node CREATED="1491392734196" ID="ID_1803941699" MODIFIED="1491392796510" TEXT="To calculate T [r] groupOpH z^{-c}: c must be efficiently invertable"/>
</node>
</node>
<node CREATED="1491996126345" ID="ID_1886564204" MODIFIED="1491996131902" TEXT="Hamilton cycle">
<node CREATED="1491996132913" ID="ID_1223181920" MODIFIED="1491996201690" TEXT="Either reveal permuted cycle"/>
<node CREATED="1491996203894" ID="ID_364009807" MODIFIED="1491996228208" TEXT="Or reveal permutation matrix to original"/>
</node>
<node CREATED="1491996454207" ID="ID_1536679025" MODIFIED="1491996459292" TEXT="commitment scheme">
<node CREATED="1491996460388" ID="ID_712266367" MODIFIED="1491996462555" TEXT="Phases">
<node CREATED="1491996469873" ID="ID_1412486405" MODIFIED="1491996475589" TEXT="commit">
<node CREATED="1491996548406" ID="ID_941753485" MODIFIED="1491996611105" TEXT="Peggy generates a blob b=c(Information,randomness)"/>
<node CREATED="1491999900062" ID="ID_907008430" MODIFIED="1491999905597" TEXT="Commitmet schemes">
<node CREATED="1491999907349" ID="ID_1783621117" MODIFIED="1491999914212" TEXT="GI">
<node CREATED="1492000015304" ID="ID_1937206572" MODIFIED="1492000019193" TEXT="Type H"/>
</node>
<node CREATED="1491999914493" ID="ID_1549754932" MODIFIED="1491999919294" TEXT="DL">
<node CREATED="1492000021089" ID="ID_1262510080" MODIFIED="1492000028482" TEXT="Type B"/>
</node>
<node CREATED="1491999919584" ID="ID_1474104447" MODIFIED="1491999923514" TEXT="Pedersen">
<node CREATED="1492000031064" ID="ID_625088873" MODIFIED="1492000033830" TEXT="Type H"/>
</node>
<node CREATED="1491999952539" ID="ID_1341072905" MODIFIED="1491999957909" TEXT="QR B">
<node CREATED="1492000036186" ID="ID_1588624154" MODIFIED="1492000043490" TEXT="Type B"/>
</node>
<node CREATED="1491999958651" ID="ID_771891122" MODIFIED="1491999962788" TEXT="QR H">
<node CREATED="1492000045346" ID="ID_625340744" MODIFIED="1492000047542" TEXT="Type H"/>
</node>
</node>
</node>
<node CREATED="1491996463989" ID="ID_1332613816" MODIFIED="1491996469296" TEXT="open"/>
</node>
<node CREATED="1491996231815" ID="ID_1764620650" MODIFIED="1491996235734" TEXT="properties">
<node CREATED="1491996237722" ID="ID_1898377727" MODIFIED="1491996242074" TEXT="correctness">
<node CREATED="1491996252661" ID="ID_386246812" MODIFIED="1491996272531" TEXT="might be called compleness"/>
</node>
<node CREATED="1491996276271" ID="ID_1756197092" MODIFIED="1491996280431" TEXT="Binding">
<node CREATED="1491996325422" ID="ID_999923582" MODIFIED="1491996334858" TEXT="might be perfect binding">
<node CREATED="1491996384224" ID="ID_1414902950" MODIFIED="1491996386769" TEXT="Type B"/>
<node CREATED="1491996708727" ID="ID_48028845" MODIFIED="1491996773320" TEXT="no exist x1!=x2,r1,r2: c(x1,r1)=c(x2,r2)"/>
<node CREATED="1491999746576" ID="ID_72798342" MODIFIED="1491999756146" TEXT="no trapdoor exists"/>
</node>
</node>
<node CREATED="1491996280753" ID="ID_704478595" MODIFIED="1491996283314" TEXT="hiding">
<node CREATED="1491996287831" ID="ID_1347275783" MODIFIED="1491996294725" TEXT="Computational hiding"/>
<node CREATED="1491996310222" ID="ID_996960773" MODIFIED="1491996316868" TEXT="statistical binding"/>
<node CREATED="1491996337624" ID="ID_212015400" MODIFIED="1491996369462" TEXT="might be perfect hiding (unless already perfect binding)">
<node CREATED="1491996373708" ID="ID_1586633620" MODIFIED="1491996379353" TEXT="Type H"/>
<node CREATED="1491996780766" ID="ID_694654968" MODIFIED="1491998721384" TEXT="any x1!=x2: c(x1,.) with same distribution c(x2,.)"/>
</node>
<node CREATED="1491999768079" ID="ID_1446267999" MODIFIED="1491999774608" TEXT="trapdoor might exist"/>
</node>
<node CREATED="1491998408888" ID="ID_1980375406" MODIFIED="1491998412038" TEXT="trapdoor">
<node CREATED="1491998413860" ID="ID_1935857300" MODIFIED="1491998430646" TEXT="I can make up a blob that I can open in both ways"/>
<node CREATED="1491998547223" ID="ID_675652438" MODIFIED="1491998557690" TEXT="can be made zero knowledge"/>
</node>
</node>
</node>
</node>
<node CREATED="1493810628715" ID="ID_731551708" MODIFIED="1493810641882" POSITION="right" TEXT="Mukti-Party Computation">
<node CREATED="1493810699654" ID="ID_92397186" MODIFIED="1493810703421" TEXT="Terms">
<node CREATED="1493810704558" ID="ID_933572721" MODIFIED="1493810708470" TEXT="Secure">
<node CREATED="1493810719322" ID="ID_1773678499" MODIFIED="1493810740353" TEXT="If bad guys can not do more in the protocol as in the specification"/>
</node>
<node CREATED="1493810649613" ID="ID_402384893" MODIFIED="1493810661527" TEXT="Specification">
<node CREATED="1493810662326" ID="ID_1119782580" MODIFIED="1493810665597" TEXT="User"/>
<node CREATED="1493810665926" ID="ID_1652737988" MODIFIED="1493810671382" TEXT="Truted Party"/>
</node>
<node CREATED="1493810673579" ID="ID_1972693808" MODIFIED="1493810677990" TEXT="Protocol">
<node CREATED="1493810777139" ID="ID_1829509855" MODIFIED="1493810779961" TEXT="User"/>
<node CREATED="1493810679412" ID="ID_1750083812" MODIFIED="1493810696800" TEXT="Simulating players">
<node CREATED="1493810764906" ID="ID_1621353945" MODIFIED="1493810775081" TEXT="Simulate trusted party"/>
</node>
<node CREATED="1493810936871" ID="ID_165987572" MODIFIED="1493810944609" TEXT="Specifies">
<node CREATED="1493810945429" ID="ID_6584567" MODIFIED="1493810947780" TEXT="Input"/>
<node CREATED="1493810948133" ID="ID_1883493402" MODIFIED="1493810950033" TEXT="COmpute"/>
<node CREATED="1493810950345" ID="ID_541443543" MODIFIED="1493810953403" TEXT="Output"/>
</node>
</node>
<node CREATED="1493811036352" ID="ID_1276231339" MODIFIED="1493811043854" TEXT="Secret Sharing Schemes">
<node CREATED="1493811044949" ID="ID_790149016" MODIFIED="1493811102208" TEXT="A small fraction of the players do not know a value but multiple players might know"/>
<node CREATED="1493811659550" ID="ID_1597144793" MODIFIED="1493811661674" TEXT="Formal">
<node CREATED="1493811664850" ID="ID_1535922300" MODIFIED="1493811669906" TEXT="Correctness">
<node CREATED="1493811613944" ID="ID_1046931523" MODIFIED="1493811710921" TEXT="After Share: There is a value s&apos; element of F and s&apos;=s if dealer is honest"/>
<node CREATED="1493811715696" ID="ID_913095632" MODIFIED="1493811753997" TEXT="after reconstruct:all players know the value"/>
</node>
<node CREATED="1493811670331" ID="ID_1788376178" MODIFIED="1493811673811" TEXT="Privacy">
<node CREATED="1493811777780" ID="ID_1561946300" MODIFIED="1493811892077" TEXT="after Share: all M not Elementt of  ?? have no info on S"/>
</node>
</node>
<node CREATED="1493812771296" ID="ID_1703744430" MODIFIED="1493812774816" TEXT="example">
<node CREATED="1493812776897" ID="ID_1032193476" MODIFIED="1493812785922" TEXT="Shamir Sharing sheme">
<node CREATED="1493812861567" ID="ID_9614987" MODIFIED="1493812894587" TEXT="choose a poly function of degree t and (f0)=s"/>
<node CREATED="1493812896629" ID="ID_43321305" MODIFIED="1493812951232" TEXT="give to any party a point on the function F(x) whereas x!=0">
<node CREATED="1493813003136" ID="ID_215033955" MODIFIED="1493813026931" TEXT="Any party might then use lagrange interpolation to rebuild f(x0)">
<node CREATED="1493814087114" ID="ID_877788941" MODIFIED="1493814162099" TEXT="s=sum(i=1..n, omega_i s_i">
<node CREATED="1493814164320" ID="ID_613557392" MODIFIED="1493814307417" TEXT="omega_i=prod(j=1..(i-1),(i+1)..n; -alpha_j/(a_i-alpha_j)"/>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
</map>
