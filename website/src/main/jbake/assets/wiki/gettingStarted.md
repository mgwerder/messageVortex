Introduction
============
MessageVortex is an academic research project aimed to get maximum of unobservability when communicating over today's networks. We achieve this by hiding within other protocols. This special feature allows us to work without a MessageVortex specific infrastructure. Instead, we can hide our traffic within normal internet services.

MessageVortex has a stack of four layers:
- The transport layers is a connector module to an internet service, which allows us to transport our VortexMesages.
- The blending layer hides the VortexMessage within our specified transport protocol.
- The routing layer handles the routing of VortexMessages.
- The accounting layer handles all the housekeeping of the routing layer and guarantees that the system as a whole remains unaffected by MessageVortex.