package uniandes.dpoo.hamburguesas.consola;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import uniandes.dpoo.hamburguesas.excepciones.HamburguesaException;
import uniandes.dpoo.hamburguesas.excepciones.NoHayPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.excepciones.YaHayUnPedidoEnCursoException;
import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.Ingrediente;
import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.ProductoAjustado;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
import uniandes.dpoo.hamburguesas.mundo.Restaurante;

/**
 * Consola interactiva para gestionar el restaurante de hamburguesas.
 */
public class AplicacionHamburguesas
{
    private static final String RUTA_INGREDIENTES = "data/ingredientes.txt";
    private static final String RUTA_MENU = "data/menu.txt";
    private static final String RUTA_COMBOS = "data/combos.txt";

    private Restaurante restaurante;
    private Scanner scanner;

    public AplicacionHamburguesas( )
    {
        restaurante = new Restaurante( );
        scanner = new Scanner( System.in );
    }

    public static void main( String[] args )
    {
        AplicacionHamburguesas aplicacion = new AplicacionHamburguesas( );
        aplicacion.ejecutar( );
    }

    private void ejecutar( )
    {
        if( !cargarDatosIniciales( ) )
        {
            return;
        }

        boolean continuar = true;
        while( continuar )
        {
            imprimirOpciones( );
            String opcion = scanner.nextLine( ).trim( );
            switch( opcion )
            {
                case "1":
                    mostrarMenu( );
                    break;
                case "2":
                    iniciarPedido( );
                    break;
                case "3":
                    agregarProductoAPedido( );
                    break;
                case "4":
                    cerrarPedido( );
                    break;
                case "5":
                    consultarPedidoPorId( );
                    break;
                case "6":
                    continuar = false;
                    System.out.println( "Hasta pronto." );
                    break;
                default:
                    System.out.println( "Opción no válida. Intente de nuevo." );
                    break;
            }
        }

        scanner.close( );
    }

    private boolean cargarDatosIniciales( )
    {
        try
        {
            restaurante.cargarInformacionRestaurante( new File( RUTA_INGREDIENTES ), new File( RUTA_MENU ),
                    new File( RUTA_COMBOS ) );
            return true;
        }
        catch( HamburguesaException | IOException e )
        {
            System.err.println( "No fue posible cargar la información inicial: " + e.getMessage( ) );
        }
        catch( NumberFormatException e )
        {
            System.err.println( "Los archivos de datos tienen un formato inválido: " + e.getMessage( ) );
        }
        return false;
    }

    private void imprimirOpciones( )
    {
        System.out.println( "\n==============================" );
        System.out.println( "Menú principal" );
        System.out.println( "1. Mostrar menú" );
        System.out.println( "2. Iniciar nuevo pedido" );
        System.out.println( "3. Agregar elemento a pedido" );
        System.out.println( "4. Cerrar pedido y guardar factura" );
        System.out.println( "5. Consultar pedido por identificador" );
        System.out.println( "6. Salir" );
        System.out.print( "Seleccione una opción: " );
    }

    private void mostrarMenu( )
    {
        System.out.println( "\n--- Productos básicos ---" );
        List<ProductoMenu> menuBase = restaurante.getMenuBase( );
        for( int i = 0; i < menuBase.size( ); i++ )
        {
            ProductoMenu producto = menuBase.get( i );
            System.out.printf( "%d. %s - $%d%n", i + 1, producto.getNombre( ), producto.getPrecio( ) );
        }

        System.out.println( "\n--- Combos ---" );
        List<Combo> combos = restaurante.getMenuCombos( );
        for( int i = 0; i < combos.size( ); i++ )
        {
            Combo combo = combos.get( i );
            System.out.printf( "%d. %s - $%d%n", i + 1, combo.getNombre( ), combo.getPrecio( ) );
        }
    }

    private void iniciarPedido( )
    {
        try
        {
            System.out.print( "Nombre del cliente: " );
            String nombre = scanner.nextLine( ).trim( );
            System.out.print( "Dirección del cliente: " );
            String direccion = scanner.nextLine( ).trim( );
            restaurante.iniciarPedido( nombre, direccion );
            System.out.println( "Pedido iniciado correctamente." );
        }
        catch( YaHayUnPedidoEnCursoException e )
        {
            System.out.println( "No es posible iniciar un nuevo pedido: " + e.getMessage( ) );
        }
    }

    private void agregarProductoAPedido( )
    {
        Pedido pedido = restaurante.getPedidoEnCurso( );
        if( pedido == null )
        {
            System.out.println( "No hay un pedido en curso. Debe iniciar uno primero." );
            return;
        }

        System.out.println( "Seleccione el tipo de producto a agregar:" );
        System.out.println( "1. Producto del menú" );
        System.out.println( "2. Combo" );
        System.out.println( "3. Producto ajustado" );
        System.out.print( "Opción: " );

        String opcion = scanner.nextLine( ).trim( );
        switch( opcion )
        {
            case "1":
                ProductoMenu productoMenu = seleccionarProductoMenu( );
                if( productoMenu != null )
                {
                    pedido.agregarProducto( productoMenu );
                    System.out.println( "Producto agregado." );
                }
                break;
            case "2":
                Combo combo = seleccionarCombo( );
                if( combo != null )
                {
                    pedido.agregarProducto( combo );
                    System.out.println( "Combo agregado." );
                }
                break;
            case "3":
                ProductoAjustado ajustado = crearProductoAjustado( );
                if( ajustado != null )
                {
                    pedido.agregarProducto( ajustado );
                    System.out.println( "Producto ajustado agregado." );
                }
                break;
            default:
                System.out.println( "Opción no válida." );
                break;
        }
    }

    private ProductoMenu seleccionarProductoMenu( )
    {
        List<ProductoMenu> menuBase = restaurante.getMenuBase( );
        if( menuBase.isEmpty( ) )
        {
            System.out.println( "No hay productos en el menú." );
            return null;
        }

        for( int i = 0; i < menuBase.size( ); i++ )
        {
            ProductoMenu producto = menuBase.get( i );
            System.out.printf( "%d. %s - $%d%n", i + 1, producto.getNombre( ), producto.getPrecio( ) );
        }
        System.out.print( "Seleccione el número del producto: " );

        try
        {
            int indice = Integer.parseInt( scanner.nextLine( ).trim( ) );
            if( indice < 1 || indice > menuBase.size( ) )
            {
                System.out.println( "Selección inválida." );
                return null;
            }
            return menuBase.get( indice - 1 );
        }
        catch( NumberFormatException e )
        {
            System.out.println( "Debe ingresar un número válido." );
            return null;
        }
    }

    private Combo seleccionarCombo( )
    {
        List<Combo> combos = restaurante.getMenuCombos( );
        if( combos.isEmpty( ) )
        {
            System.out.println( "No hay combos disponibles." );
            return null;
        }

        for( int i = 0; i < combos.size( ); i++ )
        {
            Combo combo = combos.get( i );
            System.out.printf( "%d. %s - $%d%n", i + 1, combo.getNombre( ), combo.getPrecio( ) );
        }
        System.out.print( "Seleccione el número del combo: " );

        try
        {
            int indice = Integer.parseInt( scanner.nextLine( ).trim( ) );
            if( indice < 1 || indice > combos.size( ) )
            {
                System.out.println( "Selección inválida." );
                return null;
            }
            return combos.get( indice - 1 );
        }
        catch( NumberFormatException e )
        {
            System.out.println( "Debe ingresar un número válido." );
            return null;
        }
    }

    private ProductoAjustado crearProductoAjustado( )
    {
        ProductoMenu base = seleccionarProductoMenu( );
        if( base == null )
        {
            return null;
        }

        ProductoAjustado ajustado = new ProductoAjustado( base );
        boolean ajustando = true;
        while( ajustando )
        {
            System.out.println( "\nOpciones de ajuste:" );
            System.out.println( "1. Agregar ingrediente" );
            System.out.println( "2. Eliminar ingrediente" );
            System.out.println( "3. Terminar ajustes" );
            System.out.print( "Seleccione una opción: " );

            String opcion = scanner.nextLine( ).trim( );
            switch( opcion )
            {
                case "1":
                    Ingrediente ingredienteAgregado = seleccionarIngrediente( );
                    if( ingredienteAgregado != null )
                    {
                        ajustado.agregarIngrediente( ingredienteAgregado );
                        System.out.println( ingredienteAgregado.getNombre( ) + " agregado." );
                    }
                    break;
                case "2":
                    Ingrediente ingredienteEliminado = seleccionarIngrediente( );
                    if( ingredienteEliminado != null )
                    {
                        ajustado.eliminarIngrediente( ingredienteEliminado );
                        System.out.println( ingredienteEliminado.getNombre( ) + " marcado para eliminar." );
                    }
                    break;
                case "3":
                    ajustando = false;
                    break;
                default:
                    System.out.println( "Opción no válida." );
                    break;
            }
        }
        return ajustado;
    }

    private Ingrediente seleccionarIngrediente( )
    {
        List<Ingrediente> ingredientes = restaurante.getIngredientes( );
        if( ingredientes.isEmpty( ) )
        {
            System.out.println( "No hay ingredientes registrados." );
            return null;
        }

        for( int i = 0; i < ingredientes.size( ); i++ )
        {
            Ingrediente ingrediente = ingredientes.get( i );
            System.out.printf( "%d. %s - $%d%n", i + 1, ingrediente.getNombre( ), ingrediente.getCostoAdicional( ) );
        }
        System.out.print( "Seleccione el número del ingrediente: " );

        try
        {
            int indice = Integer.parseInt( scanner.nextLine( ).trim( ) );
            if( indice < 1 || indice > ingredientes.size( ) )
            {
                System.out.println( "Selección inválida." );
                return null;
            }
            return ingredientes.get( indice - 1 ); 
        }
        catch( NumberFormatException e )
        {
            System.out.println( "Debe ingresar un número válido." );
            return null;
        }
    }

    private void cerrarPedido( )
    {
        Pedido pedido = restaurante.getPedidoEnCurso( );
        if( pedido == null )
        {
            System.out.println( "No hay un pedido en curso para cerrar." );
            return;
        }

        int idPedido = pedido.getIdPedido( );
        try
        {
            restaurante.cerrarYGuardarPedido( );
            System.out.println( "Pedido cerrado. Factura guardada en facturas/factura_" + idPedido + ".txt" );
            System.out.println( "Resumen del pedido:" );
            System.out.println( pedido.generarTextoFactura( ) );
        }
        catch( NoHayPedidoEnCursoException e )
        {
            System.out.println( "No hay pedido en curso." );
        }
        catch( IOException e )
        {
            System.out.println( "No fue posible guardar la factura: " + e.getMessage( ) );
        }
    }

    private void consultarPedidoPorId( )
    {
        System.out.print( "Ingrese el identificador del pedido: " );
        try
        {
            int idPedido = Integer.parseInt( scanner.nextLine( ).trim( ) );
            Pedido pedido = restaurante.buscarPedidoPorId( idPedido );
            if( pedido == null )
            {
                System.out.println( "No existe un pedido con ese identificador." );
                return;
            }

            System.out.println( "Información del pedido:" );
            System.out.println( pedido.generarTextoFactura( ) );
        }
        catch( NumberFormatException e )
        {
            System.out.println( "Debe ingresar un número válido." );
        }
    }
}