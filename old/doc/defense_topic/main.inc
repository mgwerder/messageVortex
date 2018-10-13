\begin{document}

% *****************************************************************************************
% ***  Titel
% *****************************************************************************************
\begin{frame}<presentation>[label=title]{~}{}
\maketitle
\end{frame}
\teachernotes[Mitnehmen]{
\begin{itemize}
\item Poster
\item Handout
\item Notebook mit Adapter
\end{itemize}
}


% *****************************************************************************************
% ***  Inhalt
% *****************************************************************************************
\newpage
\begin{frame}<presentation>[label=toc]{Table of contents}
\begin{block}{Table of contents}
\mode<presentation>{
  \setcounter{tocdepth}{2}
  \small\begin{multicols}{2}
    \tableofcontents
  \end{multicols}
}
\end{block}
\end{frame}
\showslide{toc}

\mode<article>{\setcounter{tocdepth}{4}  
\tableofcontents}



% *****************************************************************************************
% ***  Ziele der Defense
% *****************************************************************************************
\newpage
\mode<all>{\mySection[General]{General}}
\mode<all>{\mySubsection[Goals]{Goals}}
\begin{frame}<presentation>[label=goals]{General}{Goals}
  \begin{block}{Main Goals are ...}
    \begin{itemize}
      \item ... to have a common understanding of the PhD topic.
      \item ... to have an agreement on the focus of the thesis.
      \item ... to have an agreement on the expected outcome of thesis. 
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{goals}

\teachernotes[09:20]{
  \begin{itemize}
    \item Abhohlen ob diese Ziele dem generellen Verständnis entsprechen.
  \end{itemize}
}

% *****************************************************************************************
% ***  Main Goal
% *****************************************************************************************
\newpage
\mode<all>{\mySubsection[Thesis Goal]{Thesis Goal}}
\begin{frame}<presentation>[label=tgoal]{General}{Thesis Goal}
  \begin{block}{Thesis Goal}
    Send messages unobserved through a public network.
  \end{block}
\end{frame}
\showslide{tgoal}

\teachernotes[09:20]{
\begin{itemize}
\item Abhohlen ob diese Ziele dem generellen Verständnis entsprechen.
\end{itemize}
}

% *****************************************************************************************
% ***  Definition system
% *****************************************************************************************
\newpage
\mode<all>{\mySection[Definitions]{Definititons}}
\mode<all>{\mySubsection[System]{System}}
\begin{frame}<presentation>[label=system]{Definitions}{System}
  \begin{block}{Definition of system}
    \begin{itemize}
      \item Sends messages unobserved (not perceived) through public networks.
      \item Is easy to accept for users and node owners.
      \item Is reliable (in terms of message delivery and security).
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{system}

Actually a more comprehensive list of attributes will follow in a couple of slides. But this definition should do it as a general approach.

% *****************************************************************************************
% ***  Definition user
% *****************************************************************************************
\mode<all>{\mySubsection[User]{User}}
\begin{frame}<presentation>[label=user]{Definitions}{User}
  \begin{block}{Attributes of user}
    \tiny\begin{itemize}
	  \item Does care about privacy.
      \item Does or does not have support from a mail server admin.
	  \item Has no special computer knowhow.
	  \item Has the ability to install a program or plugin on his personal computer.
	  \item Has no cryptographic knowhow.
	  \item Is using a device with enough calculation power to solve cryptographic tasks.
    \end{itemize}
  \end{block}
  \begin{block}{Intentions of user}
    \tiny\begin{itemize}
	  \item Send personal or confidential information securely to another user.
    \end{itemize}
  \end{block}
  \begin{block}{Expectations of user}
    \tiny\begin{itemize}
	  \item System should be easy to configure and maintain (in an ideal world: Zero touch). 
	  \item System should be fast.
	  \item System should be reliable.
	  \item System should work on any client he is already using.
	  \item System should not be a legal problem to him or any of his peers.
    \end{itemize}
  \end{block}
\end{frame}
\showslide{user}

% *****************************************************************************************
% ***  Definition observer
% *****************************************************************************************
\newpage
\mode<all>{\mySubsection[Observer]{Observer}}
\begin{frame}<presentation>[label=observer]{Definitions}{Observer}
  \begin{block}{Attributes of observer}
    \begin{itemize}
  	  \item Available founding is huge.
  	  \item Can have nodes infrastructure.
	  \item Is able to read, write, modify or reroute network data freely at any point of the net.
    \end{itemize}
  \end{block}
  \begin{block}{Intentions of observer}
    \begin{itemize}
	  \item Discover message flows
  	  \item Discover message contents
	  \item Identify users of the system
	  \item Collect data of of users
    \end{itemize}
  \end{block}
\end{frame}
\showslide{observer}

% *****************************************************************************************
% ***  Definition owner
% *****************************************************************************************
\mode<all>{\mySubsection[Owner]{Owner}}
\begin{frame}<presentation>[label=owner]{Definitions}{Owner}
  \begin{block}{Definition of owner}
    \tiny\begin{itemize}
      \item Does care about privacy.
	  \item Has considerable computer know-how.
	  \item Has the ability to install programs or plugins.
	  \item Has possibly no cryptographic know-how.
	  \item Does know his own infrastructure.
	  \item Is using an Infrastructure with enough calculation power to solve cryptographic tasks.
    \end{itemize}
  \end{block}  
  \begin{block}{Intentions of owner}
    \tiny\begin{itemize}
	  \item Support his users in sending personal or confidential information securely to another user
    \end{itemize}
  \end{block}  
  \begin{block}{Expectations of owner}
    \tiny\begin{itemize}
	  \item System should be easy to configure and maintain (in an ideal world: Zero touch). 
	  \item System should be fast.
	  \item System should be reliable.
	  \item System should work on any client he is using.
	  \item System should not be a legal problem for him or his company.
	  \item System should still allow him to do regulatory tasks such as virus scanning or backup.
    \end{itemize}
  \end{block}
\end{frame}
\showslide{owner}

% *****************************************************************************************
% ***  Definition node
% *****************************************************************************************
\newpage
\mode<all>{\mySubsection[Node]{Node}}
\begin{frame}<presentation>[label=node]{Definitions}{Node}
  \begin{block}{Attributes of Node}
    \begin{itemize}
	  \item Is a publicly reachable Server.
	  \item Participates in the whole system.
	  \item Serves one or more defined purposes.
	  \item Does have users participating in the unobservable system and other users.
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{node}

% *****************************************************************************************
% ***  Required Attributes (Protocol)
% *****************************************************************************************
\newpage
\mode<all>{\mySection[Requirements]{Requirements}}
\mode<all>{\mySubsection[Protocol]{Protocol}}
\begin{frame}<presentation>[label=protocol]{Requirements}{Protocol}
  \begin{block}{Protocol requirements}
    \begin{itemize}
	  \item{Unidentifiable}
      \item{Untagable}
	  \item{Unreplayable}
	  \item{Monolithic messages}
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{protocol}

\begin{itemize}
	\item{Unidentifiable}\\
		If a message or a participating node is identifiable then it is easy for an observer to block some or all parts of the system. This makes the system unreliable and may force users to use specific nodes (such as nodes which are under the control of the observer) and therefore compromise the overall security. Only a service that is able to hide its messages in legitimate network traffic is not subject to selective blocking.
	\item{Untagable}\\
		If messages going through the system are tagable by any of the participants (nodes) then an observer might tag messages and then follow them while they are propagating the network. If information is appended to a message it must be cloaked with the same reliability as the original message itself.
	\item{Unreplayable}\\
		If an observer can replay any part of the message (send it multiple times), he can identify the traffic generated by those messages by statistical means. This would enable him to identify traffic which is caused by a specific message and thus narrow down the possible final recipients.
	\item{Monolithic messages}\\
		Messages should not depend on external content (such as images). If a message is not self-contained then ``bugging'' is an easy way to identify the message on its way up until they reach the recipient or the recipient itself.
\end{itemize}

% *****************************************************************************************
% ***  Required Attributes (Infrastructure)
% *****************************************************************************************
\newpage
\mode<all>{\mySubsection[Infrastructure]{Infrastructure}}
\begin{frame}<presentation>[label=infrastructure]{Requirements}{Infrastructure}
  \begin{block}{Infrastructure requirements}
    \begin{itemize}
  	  \item{Unknown endpoints}
	  \item{No relations between single hops}
	  \item{Untrusted infrastructure}
	  \item{No central infrastructure}
	  \item{No direct communication between endpoints}
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{infrastructure}

\begin{itemize}
	\item{Unknown endpoints}\\
		Every endpoint should behave the same as an intermediate routing point. They should receive and send messages so that they are not identifiable as endpoints. Identifiable endpoints simplify analysis.
	\item{No relations between single hops}\\
		Messages transferred from server to server must be unrelated. Server identifiable to send messages due to received messages are potential targets for analysis.
	\item{Untrusted infrastructure}\\
		Unlike in a company owned net, in a public network trusting an infrastructure is not sensible. It is very often not clear who owns a server and who else does have access to it. The motivation of an infrastructure owner is often not clear and his intentions may or may not be sincere. So an unobservable system may not build its unobservability based on behaviour of the transporting infrastructure.
	\item{No central infrastructure}\\
		Central infrastructure may be attacked or shut down. They are easier to monitor than an unknown number of participants. Furthermore a central infrastructure may be used to compromise security of messages or nodes. It enables an observer to identify nodes by monitoring the traffic of a central infrastructure.
	\item{No direct communication between endpoints}\\
		If sender and receiver communicate directly then they are easily identified. So -- all communications between endpoints should normally be done via intermediate nodes.
\end{itemize} 



% *****************************************************************************************
% ***  Required Attributes (acceptance)
% *****************************************************************************************
\newpage
\mode<all>{\mySubsection[Acceptance]{Acceptance}}
\begin{frame}<presentation>[label=acceptance]{Requirements}{Acceptance}
  \begin{block}{Acceptance requirements}
    \begin{itemize}
	  \item{Easy}
	  \item{Fast}
	  \item{Reliable}
 	  \item{Not abuseable}
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{acceptance}

\begin{itemize}
	\item{Easy}\\
	      A system must be easy to use. The possibilities should be similar to common elaborated systems and the usage should be alike or the same. This offers a steep learning curve to the user.\par
	      If ignored, only the users heavily concerned about their privacy would be willing to use the system. All others would ignore it as they are not ready to invest efforts into a system that offers them not sufficient benefits but new limitations.
	\item{Fast}\\
	      In today's world we already adapted to fast moving messages. It is quite common that people talk to each other and send at the same time additional informations by chat or mail. They do expect that this information propagates fast through public networks. For some messages even an almost instant reply of the recipient is expected by the sender. Therefore any system must allow a fast transport of messages from the sender to the recipient.
	\item{Reliable}\\
	      Messages are expected to arrive at the recipient's device. Today there are numerous common systems such as email, chat, sms and mms offering reliable transfers. Any system not sending reliably will not be used due to the limitations given by an unreliable system. \par
	      Another part of reliability is the protection. The message protection must be unbreakable (within reasonable bounds). If the system can be attacked easily then it offers "no value" for "additional effort". For most users this would be a reason to discard such a solution.
	\item{Not abuseable}\\
	      Any system may be abused. The willingness of using a system if it is to easily abusable is very limited. A user will not be using a system which increases UBM (unsolicited bulk messages) or enables someone to blackmail him easily.
\end{itemize}

% *****************************************************************************************
% ***  Sneak peek
% *****************************************************************************************
\newpage
\mode<all>{\mySection[Solution]{Solution}}
\mode<all>{\mySubsection[Sneak peek]{Sneak peek}}
\begin{frame}<presentation>[label=sneak]{Solution}{Sneak peek}
  \begin{block}{Building blocks}
    \begin{itemize}
      \item Traffic/Chat generation 
      \item Steganography, encryption, and hashing
      \item cryptopuzzles
      \item Discardable identities
      \item compression
      \item One time routing tokens (for sending or error replys)
      \item Routing
      \begin{itemize}
      	\item Split and reassembly of messages 
      	\item possibly DC-Rings or XOr-trees
      	\item Onion routing
      \end{itemize}
    \end{itemize}  
  \end{block}
\end{frame}
\showslide{sneak}

\begin{frame}<presentation>[label=sneak2]{Solution}{Sneak peek (2)}
\begin{block}{Solution so far}
  \begin{itemize}\small
    \item User sends a steganographicalli hidden message to a peer. This message contains:
    \begin{itemize}
      \item Message (or parts of it) to be sent to the final recipient.
      \item Decoy traffic.
      \item OTRT (One Time Routing Token) for error messages. 
      \item Possible additional routing information.
    \end{itemize} 
    \item Node tries to decypt, uncompress, and disassemble received message into chunks.
    \item Node may reassemble chunks in the wait queue to a bigger chunk.
    \item Node may add routing information to chunk.
    \item Node passes message chunks on without knowing what is in it. It knows last and next hop (by IP).
  \end{itemize}  
\end{block}
\end{frame}
\showslide{sneak2}

% *****************************************************************************************
% ***  Thesis Title
% *****************************************************************************************
\newpage
\mode<all>{\mySection[Thesis]{Thesis}}
\mode<all>{\mySubsection[Title]{Thesis Title}}
\begin{frame}<presentation>[label=ttitle]{Thesis}{Proposed Title}
  \begin{block}{Thesis Title}
    Messagevortex -- Sending messages unobserved through a public network.
  \end{block}
\end{frame}
\showslide{ttitle}

% *****************************************************************************************
% ***  Thesis content
% *****************************************************************************************
\mode<all>{\mySubsection[Content]{Content}}
\begin{frame}<presentation>[label=thesiscontent]{Thesis}{Content}
  \begin{block}{Proposed thesis content is ...}
    \begin{itemize}\tiny
      \item Create a generic approach to transport messages through public networks unobserved. (as defined previously)
      \item Create a generic implementation (traffic generator) of the approach.
      \item Generate large scale traffic samples with different parameters used by sender.
      \item Do traffic analysis against the approach to identify weaknesses and find optimal behaviour. \\Focus lies on:
      \begin{itemize}\tiny
        \item Identifying endpoints of communication.
        \item Identifying messages, message types or parts of them.
        \item Identifying patterns of service usage.
        \item Identifying weaknesses in robustness.
      \end{itemize}
      \item Create a library/framework for creating messages.
      \item Create a working minimal prototype based on the library.
    \end{itemize}
  \end{block}
\end{frame}
\showslide{thesiscontent}

% *****************************************************************************************
% ***  Discussion
% *****************************************************************************************
\newpage
\mode<all>{\mySection[Discusion]{Discussion}}
\begin{frame}<presentation>[label=discussion]{Discussion}{}
~
\end{frame}
\showslide{discussion}

\end{document}