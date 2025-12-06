# Proyecto Final: Inventario con Base de Datos
Para este proyecto final se pusieron en marcha los conocimientos adquiridos durante el curso de Base de Datos I impartido por el profesor José Luis Aguilara Luzania.  
Se siguieron las indicaciones proporcionadas en el PDF del proyecto final en AVAUS.  
Para realizar este programa, se utilizó **Java** con el IDE **NetBeans**. Para implementar los aspectos visuales, se utilizó **JavaFX** y las interfaces gráficas se crearon manualmente. 
Para la parte de la base de datos, se utilizó el archivo proporcionado en el material del curso. Se editó y se le añadió información nueva, la más importante, la tabla de usuarios, que incluía la información de las contraseñas encriptadas, las cuales se lograron a partir de la librería jbcrypt-0.4.  


## Pantallas
<img width="596" height="425" alt="CompUson-Login" src="https://github.com/user-attachments/assets/e949a1a0-7d81-428c-b405-8f8d958775a1" />  \
Primera pantalla al abrir el programa. Una pantalla de login que acepta a los tres usuarios preregistrados. No importa si los caracteres del usuario son mayúsculas o minúsculas, pero la contraseña sí.  

  
<img width="797" height="586" alt="Compuson-Inicio" src="https://github.com/user-attachments/assets/0511d800-a264-4f36-940f-414230cefbe4" />  \
Pantalla de inicio una vez iniciada la sesión. Desde aquí se puede acceder al resto del programa.  
  
  
<img width="797" height="589" alt="CompUson-Productos" src="https://github.com/user-attachments/assets/f1f916a3-1c65-4090-af27-f9fd4ae5caf8" />  \
Pantalla de productos mostrando parte de la Base de Datos. Al hacer click en su respectiva columna, se pueden ordenar con base en su ID, nombre, etc. Se puede apreciar quién fue el último usuario en modificar cada producto.  
  

<img width="797" height="590" alt="CompUson-ProductosFiltro" src="https://github.com/user-attachments/assets/b72ae2f9-5527-41fa-a5a2-e6c287ee3f46" />  \
Filtros aplicados para que solo se vean los productos en un almacén recién creado.  


<img width="796" height="587" alt="CompUson-ProductoModificar" src="https://github.com/user-attachments/assets/cde980c9-94f7-4b85-ade5-9b33a4d32b3c" /> \
Formulario para modificar entradas de productos.  
  
  
<img width="796" height="587" alt="CompUson-Almacenes" src="https://github.com/user-attachments/assets/5d0a809e-010f-4336-85ee-29999567faf3" /> \
Pantalla de almacenes, la cual no muestra botones porque había ingresado como usuario Productos.  


## Dificultades
El hacer que los combobox se actualizaran de manera automática debido a que primero los actualizaba con un método estático que hacía que los filtros no funcionaran apropiadamente.  

## Conclusión
Este es el programa más complejo y robusto que he hecho, no solo este semestre o en la universidad, sino en mi vida entera. Tuve que aprender mucho sobre base de datos, encriptación, filtros e incluso temas que no vimos en el semestre. Estoy contento de poder haber logrado un programa que se ve bien y que tiene una funcionalidad que puede utilizarse en un negocio real.



