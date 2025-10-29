package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

public class ProductoMenuTest {
	private ProductoMenu producto1;

    @BeforeEach
    void setUp( ) throws Exception
    {
        producto1 = new ProductoMenu( "papas especiales", 12000);
    }

    @AfterEach
    void acabarcontodo( ) throws Exception
    {
    }

    @Test
    void testGetNombre( )
    {
        assertEquals( "papas especiales", producto1.getNombre( ), "El nombre del producto no es el esperado." );
    }
    
    @Test
    void testGetPrecio( )
    {
        assertEquals( 12000, producto1.getPrecio( ), "El precio del producto no es el esperado." );
    }
    @Test
    void generarTextoFacturaTest() {
        
        

        String esperado = ""
            + "papas especiales\n"
            
            + "            12000\n";

        assertEquals(esperado, producto1.generarTextoFactura());
    }}

