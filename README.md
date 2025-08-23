# Sistema de Gestao de Pacientes - Trabalho de SD

## Consideracoes de arquitetura
* Sendo este um projecto de sistemas distribuídos, os módulos [cliente](./src/client/) e [servidor](./src/server/) podem ser colocados em computadores distintos.
* Garanta que cada máquina possua a cópia correspondente de módulo [comum](./build/common/).

* Para que o script de compilação funcione após a divisão física, garanta que cada máquina, os módulos estejam no directório *./src* do directório raiz do projecto.

Numa estrutra como as indicadas abaixo:

### No Cliente
    raiz *
         |-src/
         |   |- client/
         |   |- common/
         |-client.properties
         |-build.ps1

### No Servidor
    raiz *
         |-src/
         |   |- common/
         |   |- server/
         |-server.properties
         |-build.ps1


## Dependencias

* **Obs:** Do momento somente tem build script para *powershell*
* Java 8+
* [SQLite](./lib/sqlite-jdbc-3.50.2.0.jar) 

## Configuracoes

### `server.properties`
```conf
server.port=\<porta\>
db.file=\<nomebd\>
```
### `server.properties`:
```conf
* server.port=\<porta\>
* server.host=\<ip\>
```

## Compilar e rodar

No directorio raiz correspondente execute

**Server:**
```ps1
    .\build.ps1
    java -cp "build;lib/*" server.Server
```

**Cliente:**
```ps1
    .\build.ps1
    java -cp "build;lib/*" client.ui.ClientGUI
```
