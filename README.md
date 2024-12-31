# EmerixeProxy - Minecraft (Proxy du Serveur Emerixe)

**[EmerixeProxy](https://github.com/EmerixeMinecraft/EmerixeProxy)** est un proxy permettant de relier les différentes machines et serveur Minecrafts du serveur Emerixe.


## Installation

Premièrement il faut compiler le fichier JAR d'EmerixeProxy.

```bash
  git clone https://github.com/EmerixeMinecraft/EmerixeProxy.git
  cd ./EmerixeProxy
  mvn clean install
```

Ensuite vous retrouverez le fichier JAR dans le répertoire **/target/**.
    
## Roadmap

- [ ]  Renvoyer les packets PacketLoginInEncryptionBegin et PacketLoginOutEncryptionBegin au Serveur et au Client respectivement lors de la connexion au Proxy
    - [ ]  PacketLoginInEncryptionBegin (packetId 15)
    - [ ]  PacketLoginOutEncryptionBegin (packetId ?) 


## Droits

Ce Proxy est la propriété exclusive d'Emerixe. Toute reproduction, modification ou distribution est strictement interdite sans autorisation écrite d'Emerixe.


##

**Copyright © 2024 Emerixe. Tous droits réservés.**

