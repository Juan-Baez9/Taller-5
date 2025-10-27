package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

public class ComboTest {

    // Helper mínima para crear items
    private ProductoMenu pm(String nombre, int precio) {
        return new ProductoMenu(nombre, precio);
    }

    @Test
    void getNombre_devuelveElNombreDelCombo() {
        ArrayList<ProductoMenu> items = new ArrayList<>();
        items.add(pm("Hamburguesa", 10000));
        Combo c = new Combo("MegaCombo", 0.2, items);

        assertEquals("MegaCombo", c.getNombre());
    }

    @Test
    void getPrecio_aplicaDescuentoSobreSuma() {
    	//ejemploporquesoysupierinteligente 
        // Suma = 10000 + 8000 + 6000 = 24000
        // Con 20% de descuento -> 24000 * (1 - 0.20) = 19200
        ArrayList<ProductoMenu> items = new ArrayList<>();
        items.add(pm("perrtoCALLEJERO", 10000));
        items.add(pm("Papas", 8000));
        items.add(pm("Gaseosa", 6000));

        Combo c = new Combo("MegaCombo", 0.20, items);

        assertEquals(19200, c.getPrecio());
    }

    @Test
    void generarTextoFactura_formatoExacto() {
        // Total = 12000 + 8000 = 20000; 25% desc -> 15000
        ArrayList<ProductoMenu> items = new ArrayList<>();
        items.add(pm("perroteCALLEJERO", 12000));
        items.add(pm("Papas", 8000));
        Combo c = new Combo("Clásico", 0.25, items);

        String esperado = ""
            + "Combo Clásico\n"
            + " Descuento: 0.25\n"
            + "            15000\n";

        assertEquals(esperado, c.generarTextoFactura());
    }}
