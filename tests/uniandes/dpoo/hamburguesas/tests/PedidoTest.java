package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.Producto;

/**
 * Tests unitarios para la clase Pedido.
 */
public class PedidoTest {

    // ======= Stub mínimo de Producto =======
    private static class ProductoStub implements Producto {
        private final String nombre;
        private final int precio;
        private final String factura; 

        ProductoStub(String nombre, int precio, String factura) {
            this.nombre = nombre;
            this.precio = precio;
            this.factura = factura;
        }

        @Override 
        public String getNombre() { 
        	return nombre; }
        
        @Override 
        public int getPrecio() { 
        	return precio; }
        
        @Override 
        public String generarTextoFactura() { 
        	return factura; }
    }

    // ======= Util: resetear contador estático para IDs predecibles =======
    @BeforeEach
    void resetNumeroPedidos() throws Exception {
        Field f = Pedido.class.getDeclaredField("numeroPedidos");
        f.setAccessible(true);
        f.setInt(null, 0);
    }

    // ======= Helpers =======
    private Producto p(String nombre, int precio) {
        // Formato libre porque Pedido solo concatena lo que devuelva el producto
        String factura = nombre + " .... " + precio + "\n";
        return new ProductoStub(nombre, precio, factura);
    }
    // ======= Tests =======

    @Test
    void getid_y_guardaCliente() {
        Pedido a = new Pedido("Alice", "Calle 1 #2-3");
        Pedido b = new Pedido("Bob", "Cra 9 #10-11");

        assertEquals(0, a.getIdPedido(), "Primer pedido debe tener id 0");
        assertEquals(1, b.getIdPedido(), "Segundo pedido debe tener id 1");
        assertEquals("Alice", a.getNombreCliente());
        assertEquals("Bob", b.getNombreCliente());
    }

    @Test
    void pedidoVacio_tieneTotalesEnCero_ygeneraFacturasincosas() {
        Pedido p = new Pedido("Cliente", "Dir");
        // Total neto = 0; IVA = 0; Total = 0
        assertEquals(0, p.getPrecioTotalPedido());

        String esperado =
            "Cliente: Cliente\n" +
            "Dirección: Dir\n" +
            "----------------\n" +
            // sin líneas de productos
            "----------------\n" +
            "Precio Neto:  0\n" +
            "IVA:          0\n" +
            "Precio Total: 0\n";
        assertEquals(esperado, p.generarTextoFactura());
    }

    @Test
    void agregarProducto_aplicaIVA19() {
        Pedido ped = new Pedido("Cliente", "Dirección");
        // precio total = 10.001; IVA = int(10.001 * 0.19) = int(1900.19) = 1900
        // debe ser 10.001 + 1900
        // Total = 11.901
        ped.agregarProducto(p("pizza", 10001));
        assertEquals(11901, ped.getPrecioTotalPedido());
    }

    @Test
    void genera_factura_correcta() {
        Pedido ped = new Pedido("Juan", "Cra 1 #2-3");
        // Neto = 12000 + 8000 = 20000; IVA = 3800; Total = 23800
        ped.agregarProducto(p("Hamburguesa", 12000));
        ped.agregarProducto(p("Papas", 8000));

        String esperado =
            "Cliente: Juan\n" +
            "Dirección: Cra 1 #2-3\n" +
            "----------------\n" +
            "Hamburguesa .... 12000\n" +
            "Papas .... 8000\n" +
            "----------------\n" +
            "Precio Neto:  20000\n" +
            "IVA:          3800\n" +
            "Precio Total: 23800\n";

        assertEquals(esperado, ped.generarTextoFactura());
    }
    
   
  
    @Test
    void guardarFactura_escribeElArchivoConElContenidoEsperado(@TempDir Path temp) throws IOException {
        Pedido ped = new Pedido("Ana", "Cll 5 #6-7");
        ped.agregarProducto(p("Gaseosa", 3000));
        ped.agregarProducto(p("Perro", 7000));
        // Neto 10.000; IVA 1.900; Total 11.900
        String esperado =
            "Cliente: Ana\n" +
            "Dirección: Cll 5 #6-7\n" +
            "----------------\n" +
            "Gaseosa .... 3000\n" +
            "Perro .... 7000\n" +
            "----------------\n" +
            "Precio Neto:  10000\n" +
            "IVA:          1900\n" +
            "Precio Total: 11900\n";

        File out = temp.resolve("factura.txt").toFile();
        try {
            ped.guardarFactura(out);
        } catch (Exception e) {
            fail("No debería lanzar excepción al guardar: " + e.getMessage());
        }

        String contenido = Files.readString(out.toPath(), StandardCharsets.UTF_8);
        assertEquals(esperado, contenido);
    }
}
