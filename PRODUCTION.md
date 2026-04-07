Abrir dos terminales
En la terminar 1 poner 
scp C:\Users\garci\Documents\Code\PAW-2026Q1-G2\webapp\target\vinyland.war mgarciapuente@pampero.itba.edu.ar:/home/mgarciapuente/

En la terminal 2 poner 
ssh mgarciapuente@pampero.itba.edu.ar
sftp paw-2026a-02@10.16.1.110
put vinyland.war web/app.war
Poner la contraseña que esta en el campus
exit
psql -h 10.16.1.110 -U paw-2026a-02
Poner la contraseña que esta en el campus