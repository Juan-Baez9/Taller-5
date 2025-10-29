package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import uniandes.dpoo.hamburguesas.mundo.Ingrediente;
import uniandes.dpoo.hamburguesas.mundo.ProductoAjustado;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

public class ProductoAjustadoTest {

    // --- helpers para meter ingredientes en las listas privadas ---
    @SuppressWarnings("unchecked")
    private ArrayList<Ingrediente> refLista(Object obj, String campo) throws Exception {
        Field f = obj.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (ArrayList<Ingrediente>) f.get(obj);
    }

    private void addAgregado(ProductoAjustado pa, Ingrediente ing) throws Exception {
        refLista(pa, "agregados").add(ing);
    }

    private void addEliminado(ProductoAjustado pa, Ingrediente ing) throws Exception {
        refLista(pa, "eliminados").add(ing);
    }

    //  TESTSSSSs !!

    @Test
    void sinAjustes_precioIgualAlBase_yFacturaMuestraSoloBase() {
        ProductoMenu base = new ProductoMenu("Hamburguesa", 12000);
        ProductoAjustado pa = new ProductoAjustado(base);

        assertEquals(12000, pa.getPrecio(), "Sin agregados, el precio debe ser el del producto base");

        String factura = pa.generarTextoFactura();
        
        assertTrue(factura.contains("Hamburguesa"), "La factura debe incluir el producto base");
        assertFalse(factura.contains("+"), "No debería listar agregados");
        assertFalse(factura.contains("-"), "No debería listar eliminados");
    }

    @Test
    void agregados_incrementanPrecio_sumandoCostos() throws Exception {
        ProductoMenu base = new ProductoMenu("Hamburguesa", 10000);
        ProductoAjustado pa = new ProductoAjustado(base);

        Ingrediente queso = new Ingrediente("Queso", 1500);
        Ingrediente tocineta = new Ingrediente("Tocineta", 2000);

        addAgregado(pa, queso);
        addAgregado(pa, tocineta);

        // Esperado = 10000 + 1500 + 2000 = 13500
        assertEquals(13500, pa.getPrecio(), "El precio debe ser el base + agregados");
    }

    @Test
    void agregados_duplicados_sumanVariasVeces() throws Exception {
        ProductoMenu base = new ProductoMenu("Hamburguesa", 9000);
        ProductoAjustado pa = new ProductoAjustado(base);

        Ingrediente queso = new Ingrediente("Queso", 500);
        addAgregado(pa, queso);
        addAgregado(pa, queso); // mismo ingrediente dos veces

        // Esperado = 9000 + 500 + 500 = 10000
        assertEquals(10000, pa.getPrecio(), "Duplicar agregado debe sumar dos veces su costo");
    }

    @Test
    void eliminados_noCambianElPrecio() throws Exception {
        ProductoMenu base = new ProductoMenu("Hamburguesa", 9000);
        ProductoAjustado pa = new ProductoAjustado(base);

        Ingrediente cebolla = new Ingrediente("Cebolla", 300);
        addEliminado(pa, cebolla);

      
        assertEquals(9000, pa.getPrecio(), "Quitar ingredientes no cambia el precio");
    }

    @Test
    void factura_listaAgregadosConMas_yCosto_eliminadosConMenos_yTotalCorrecto() throws Exception {
        ProductoMenu base = new ProductoMenu("Hamburguesa", 8000);
        ProductoAjustado pa = new ProductoAjustado(base);

        Ingrediente queso = new Ingrediente("Queso", 1000);
        Ingrediente pepinillos = new Ingrediente("Pepinillos", 700);
        Ingrediente cebolla = new Ingrediente("Cebolla", 300);

        addAgregado(pa, queso);
        addAgregado(pa, pepinillos);
        addEliminado(pa, cebolla);

        // Total esperado = 8000 + 1000 + 700 = 9700
        String factura = pa.generarTextoFactura();

        // Verificamos que los agregados y eliminados se imprimen correctamente en la factura
        assertTrue(factura.contains("+Queso"), "Debe listar agregados con '+'");
        assertTrue(factura.contains("1000"), "Debe mostrar costo del agregado");
        assertTrue(factura.contains("+Pepinillos"), "Debe listar todos los agregados");
        assertTrue(factura.contains("700"), "Debe mostrar costo del otro agregado");
        assertTrue(factura.contains("-Cebolla"), "Debe listar eliminados con '-'");
        assertTrue(factura.contains("9700"), "Debe incluir el total correcto");
    }
}
