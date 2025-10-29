package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.excepciones.NoHayPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.excepciones.YaHayUnPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.Producto;

import uniandes.dpoo.hamburguesas.mundo.Restaurante;

public class RestauranteTest {

    private Restaurante restaurante;

    
    private static class ProductoTest implements Producto {
        private final String nombre;
        private final int precio;
        ProductoTest(String nombre, int precio) { this.nombre = nombre; this.precio = precio; }
        @Override 
        public String getNombre() { 
        	return nombre; }
        @Override 
        public int getPrecio() { 
        	return precio; }
        @Override 
        public String generarTextoFactura() { 
        	return nombre + " .... " + precio + "\n"; }
    }


    private static final Path FACTURAS_DIR = Paths.get("facturas");

    private static void ensureFacturasDir() {
        try {
            Files.createDirectories(FACTURAS_DIR);
        } catch (IOException e) {
            fail("No se pudo crear el directorio 'facturas': " + e.getMessage());
        }
    }

    private static void cleanFacturasDir() {
        ensureFacturasDir();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(FACTURAS_DIR, "factura_*.txt")) {
            for (Path p : ds) Files.deleteIfExists(p);
        } catch (IOException ignore) { }
    }

    private static int countFacturas() {
        ensureFacturasDir();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(FACTURAS_DIR, "factura_*.txt")) {
            int c = 0; for (Path ignored : ds) c++; 
            return c;
        } catch (IOException e) { return 0; }
    }

  
    @BeforeEach
    void setUp() {
        restaurante = new Restaurante();
        cleanFacturasDir();   
    }

    

    @Test
    void TestIniciarPedido() {
        assertDoesNotThrow(() -> restaurante.iniciarPedido("jose", "Calle 25c 80c 54"));
        Pedido pedido = restaurante.getPedidoEnCurso();

        assertNotNull(pedido, "Debe existir al menos un pedido en curso");
        assertEquals("jose", pedido.getNombreCliente(), "Nombre del cliente no coincide");
    }

    @Test
    void TestExcepcion() throws YaHayUnPedidoEnCursoException {
        restaurante.iniciarPedido("ronaldou", "sucasa");
        assertThrows(YaHayUnPedidoEnCursoException.class, () -> restaurante.iniciarPedido("juan", "micasa"));
    }

    @Test
    void TestGuardaryCerrar() {
        assertThrows(NoHayPedidoEnCursoException.class,
            () -> restaurante.cerrarYGuardarPedido());
    }

    @Test
    void TestSinPedidoYArchivo() {
        assertDoesNotThrow(() -> restaurante.iniciarPedido("carlos", "bosadondelagentegoza"));
        Pedido p = restaurante.getPedidoEnCurso();
        p.agregarProducto(new ProductoTest("perro mosntruo", 16000));

        int beforeFiles = countFacturas();
        assertDoesNotThrow(() -> restaurante.cerrarYGuardarPedido());

        
        assertNull(restaurante.getPedidoEnCurso(), "Después de cerrar no debe de haber pedido en curso");

        
        ArrayList<Pedido> lista = restaurante.getPedidos();
        int afterFiles = countFacturas();

        if (!lista.isEmpty()) {
            assertEquals("carlos", lista.get(lista.size()-1).getNombreCliente(),
                "El último pedido de la lista debería ser el que se cerro");
        } else {
            assertEquals(beforeFiles + 1, afterFiles,
                "Si no se usa lista interna, debe haberse creado una factura en ./facturas");
        }
    }

    @Test
    void getPedidos_devuelveListaPedidosCerrados_enOrdenOSeGeneranDosFacturas() {
        
        assertDoesNotThrow(() -> restaurante.iniciarPedido("patricio", "marsella"));
        restaurante.getPedidoEnCurso().agregarProducto(new ProductoTest("Nachos", 8000));
        int aBefore = countFacturas();
        assertDoesNotThrow(() -> restaurante.cerrarYGuardarPedido());
        int aAfter = countFacturas();

      
        assertDoesNotThrow(() -> restaurante.iniciarPedido("calamardo", "debajodelmar"));
        restaurante.getPedidoEnCurso().agregarProducto(new ProductoTest("Gaseosa", 2500));
        int bBefore = countFacturas();
        assertDoesNotThrow(() -> restaurante.cerrarYGuardarPedido());
        int bAfter = countFacturas();

        ArrayList<Pedido> pedidos = restaurante.getPedidos();

        if (pedidos.size() >= 2) {
            assertEquals("patricio", pedidos.get(pedidos.size()-2).getNombreCliente());
            assertEquals("calamardo", pedidos.get(pedidos.size()-1).getNombreCliente());
        } else {
            assertEquals(aBefore + 1, aAfter, "Debe haberse creado la factura del primer pedido");
            assertEquals(bBefore + 1, bAfter, "Debe haberse creado la factura del segundo pedido");
        }
    }

    @Test
    void pedidoEnCurso_puedeGuardarFactura_DesdePedido(@TempDir Path temp) throws IOException {
        assertDoesNotThrow(() -> restaurante.iniciarPedido("luisa", "Calle 9"));
        Pedido pedido = restaurante.getPedidoEnCurso();
        pedido.agregarProducto(new ProductoTest("Hamburguesa", 17000));

        File out = temp.resolve("factura.txt").toFile();
        pedido.guardarFactura(out);

        String factura = Files.readString(out.toPath(), StandardCharsets.UTF_8);
        String esperado =
            "Cliente: luisa\n" +
            "Dirección: Calle 9\n" +
            "----------------\n" +
            "Hamburguesa .... 17000\n" +
            "----------------\n" +
            "Precio Neto:  17000\n" +
            "IVA:          3230\n" +
            "Precio Total: 20230\n";
        assertEquals(esperado, factura);
    }

    @Test
    void getters_deMenusYIngredientes_noSonNulos() {
        assertNotNull(restaurante.getMenuBase(), "El menú base no debe ser nulo");
        assertNotNull(restaurante.getMenuCombos(), "El menú de combos no debe ser nulo");
        assertNotNull(restaurante.getIngredientes(), "La lista de ingredientes no debe ser nula");
    }

    @Test
    void todoCompleto_facturaCorrecta_DespuesDeCerrar() {
        assertDoesNotThrow(() -> restaurante.iniciarPedido("Fernando", "teusaquillo"));
        Pedido enCurso = restaurante.getPedidoEnCurso();
        enCurso.agregarProducto(new ProductoTest("Hamburguesa", 17000));
        enCurso.agregarProducto(new ProductoTest("Gaseosa", 3000));

        int beforeFiles = countFacturas();
        assertDoesNotThrow(() -> restaurante.cerrarYGuardarPedido());
        int afterFiles = countFacturas();

       
        Pedido cerrado = restaurante.getPedidos().isEmpty() ? enCurso : restaurante.getPedidos().get(0);

        String esperado =
            "Cliente: Fernando\n" +
            "Dirección: teusaquillo\n" +
            "----------------\n" +
            "Hamburguesa .... 17000\n" +
            "Gaseosa .... 3000\n" +
            "----------------\n" +
            "Precio Neto:  20000\n" +
            "IVA:          3800\n" +
            "Precio Total: 23800\n";

        assertEquals(esperado, cerrado.generarTextoFactura());

        if (restaurante.getPedidos().isEmpty()) {
            assertEquals(beforeFiles + 1, afterFiles,
                "Si no guarda en lista, debe haberse generado una factura en ./facturas");
        }
    }
}
