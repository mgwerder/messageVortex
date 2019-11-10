\usepackage[utf8]{inputenc}

\usepackage{float}

\usepackage{pgf}

\usepackage{attachfile}

\usepackage{multirow}
%\usepackage[override]{xcolor}
\usepackage{lmodern}
\usepackage[english]{babel}

\usepackage[absolute,overlay]{textpos}
\usepackage[]{eso-pic}

\usepackage{MnSymbol} % required for the arrow
\usepackage{listings}
\lstset{ %
	backgroundcolor=\color{lightgray},   
	language=java,
	frame=single,
  numbers=left,
  numbersep=5pt,
  numberstyle=\tiny
}

%puts a graphic at the absolute position described by the grid
%#1 x, #2 y, #3 width, #4 height, #5 graphic
\newcommand\putpic[6]{%
  \begin{textblock}{#3}(#1,#2)
    \includegraphics[width=#3\TPHorizModule,height=#4\TPVertModule]{#5}\\\tiny #6
  \end{textblock}
} 

\title[Defense]{Topic Defense PhD Martin Gwerder}
\subtitle{Sending Unobservable Messages Across Public Networks}
\author{Martin Gwerder}
\date{8.6.2015}

\setlength{\parindent}{0pt}
\setlength{\parskip}{1ex}
\setlength{\itemsep}{.1ex}

\makeatletter
\newcommand{\currentsection}{\@nameuse{\thesection}}
\newcommand{\currentsubsection}{\@nameuse{\thesubsection}}
\newcommand{\mySection}[2][]{%
      \section<presentation>[#1]{#2}
      \section<article>{#2}
      \@namedef{\thesection}{#2}
}

\newcommand\mySubsection[2][]{%
      \subsection<presentation>[#1]{#2}
      \subsection<article>{#2}
      \@namedef{\thesubsection}{#2}
}

\newcommand\mySubsubsection[2][]{%
      \subsubsection<presentation>[#1]{#2}
      \subsubsection<article>{#2}
}
\makeatother

\newcommand{\caution}{\marginpar{\center\includegraphics[width=1cm]{../symb_caution}}}
\newcommand{\joker}{\marginpar{\center\includegraphics[width=1cm]{symb_joker}}}
\newcommand{\work}{\marginpar{\center\includegraphics[width=1cm]{symb_work}}}

\usepackage{datetime}
\usepackage{hyperref}
\makeatletter
\hypersetup{
	hidelinks,
  pdfpagelayout=TwoPageRight,
  pdfstartview=Fit
  driver=pdftex,
  pdfinfo={
    Title={\@title},
    Author={Martin Gwerder},
    Creator={PDFLaTeX},
    CreationDate={D:\pdfdate},
    Subject={Sending Unobservable Messages Across Public Networks},
    Keywords={Unobservable, Messages, Anonymity, Encryption, Steganography}
  }
}
\makeatother

