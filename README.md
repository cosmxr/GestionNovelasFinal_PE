# Gestión de Novelas

## Descripción del Proyecto

La aplicación **Gestión de Novelas** es una herramienta diseñada para ayudar a los usuarios a gestionar su biblioteca personal de novelas. Permite a los usuarios:
- Agregar nuevas novelas.
- Marcar novelas como favoritas.
- Ver reseñas de novelas.
- Añadir reseñas a cada novela.
- Filtrar novelas favoritas.

Esta aplicación está desarrollada utilizando **Jetpack Compose**, una moderna herramienta para construir interfaces de usuario en Android.

## Clases y Estructura

### 1. Clase `Novela`

Representa una novela en la aplicación.

- **Propiedades:**
  - `id`: Identificador único de la novela.
  - `titulo`: Título de la novela.
  - `autor`: Autor de la novela.
  - `año_publicacion`: Año en que se publicó la novela.
  - `descripcion`: Breve descripción de la novela.
  - `isFavorita`: Bandera que indica si la novela es favorita.

### 2. Clase `Reseña`

Representa una reseña asociada a una novela.

- **Propiedades:**
  - `novelaId`: Identificador de la novela a la que pertenece la reseña.
  - `contenido`: Texto de la reseña.

### 3. Composable `TarjetaNovela`

Muestra la información de una novela con la opción de marcarla como favorita.

- **Parámetros:**
  - `novela`: Objeto que contiene la información de la novela.
  - `onAddReviewClick`: Callback para manejar la acción de añadir reseña.
  - `onToggleFavoriteClick`: Callback para manejar la acción de marcar/desmarcar como favorita.

### 4. Composable `NovelListScreen`

Muestra una lista de novelas y proporciona opciones para agregar nuevas novelas, ver reseñas y favoritas.

- **Parámetros:**
  - `navController`: Controlador de navegación para manejar la navegación entre pantallas.
  - `novelas`: Lista de objetos que representan las novelas a mostrar.
  - `onNovelasUpdated`: Callback para manejar actualizaciones en la lista de novelas.

### 5. Composable `FavoriteNovelsScreen`

Muestra una lista de novelas que han sido marcadas como favoritas.

- **Parámetros:**
  - `navController`: Controlador de navegación para manejar la navegación entre pantallas.
  - `novelas`: Lista de objetos que representan las novelas que se filtrarán para mostrar solo las favoritas.

### 6. Función `toggleFavorite`

Función auxiliar para marcar o desmarcar una novela como favorita.

- **Parámetros:**
  - `novela`: Objeto que se va a marcar o desmarcar.
  - `novelas`: Lista actual de novelas.
  - `onNovelasUpdated`: Callback para actualizar la lista de novelas después de cambiar el estado de favoritos.

link al repositorio -> https://github.com/cosmxr/GestionNovelasFinal_PE.git
