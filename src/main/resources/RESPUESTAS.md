# Solución

## Karol Ximena Rodriguez Reyes
## Juan David Moreno D'Aleman

## Parte I

![alt text](/imagenes/jVisual.png)

**1.** Este consumo aunque no esta tan alto, se podria ver en la clase `Consumer`, ya que a pesar de que el productor agrega un elemento cada segundo, esta clase esta ejecutando todo el tiempo:

```
while (true) {

            if (queue.size() > 0) {
                int elem=queue.poll();
                System.out.println("Consumer consumes "+elem);                                
            }
            
        }
```

Por lo que si la cola esta vaia, el consumidor de todas maneras seguira preguntando sin parar.

![alt text](/imagenes/image.png)

**2.** para hacer mas eficiente, lo que hacemos es implementar `synchronized` en ambas clases, la de productor y la del consumidor.

Primero, en la clase `Consumer` la condición de espera estara dentro de un while para que el hilo pueda despertarse y encontrar que la cola sigue vacia, por lo que verificamos si la cola esta vacia, en ese caso usamos `wait()` para suspender el consumidor y libera el bloqueo de queue para que el productor entre y agregue elementos.

Ahora, para el productor, cuando este cree un elemento, debe agregarlo y notificar al consumidor usando queue, por lo que bloquea queue mientras agrega dico elemento. Ahi llama a `notifyAll()` para despertar al consumidor y liberar la cola.

![alt text](/imagenes/image-1.png)

Como vemos el consumo de cpu bajo considerablemente, incluso hubo momentos donde registraba 0.0% - 0.1%.

**3.** Para este punto cambiamos la velocidad de los hilos. Ahora el productor produce muy rapido, por lo que quitamos el `Thread.sleep()` de esta clase. En cambio, hacemos que el consumidor consuma lentamente agregando un `Thread.sleep(1000)` despues de consumir cada elemento.

Tambien cambiamos el tipo de cola por una `BlockingQueue` y usamos una `ArrayBlockingQueue` con capacidad para 10 elementos:

```
BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
```

Este valor representa el limite de stock. La cola no puede tener mas de 10 elementos.

En la clase `Producer` usamos `put()` para agregar los elementos. Este metodo bloquea al productor cuando la cola esta llena, hasta que el consumidor retire un elemento y haya espacio disponible. Por eso el productor no puede superar el limite establecido.

En la clase `Consumer` usamos `take()` para retirar los elementos. Este metodo bloquea al consumidor cuando la cola esta vacia, hasta que el productor agregue un nuevo elemento. De esta forma no es necesario usar una espera activa ni revisar constantemente el estado de la cola.

Con estos cambios, el productor llena rapidamente la cola hasta alcanzar los 10 elementos y luego espera. El consumidor retira un elemento cada segundo, permitiendo que el productor continue solamente cuando se libera un espacio. Al ejecutar la aplicacion y observarla en jVisualVM, se debe verificar que la cola no supere el limite y que no se presente un consumo alto de CPU, ya que ambos hilos quedan bloqueados cuando no pueden continuar.

![alt text](/imagenes/image-2.png)

Como se puede ver, el uso de CPU no aumento casi llegando maximo a picos de 1.9%, por lo que no gasta tanta CPU ni hubo ningun error durante el proceso.