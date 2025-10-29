package uniandes.dpoo.hamburguesas.mundo;

import java.util.ArrayList;

/**
 * Un producto ajustado es un producto para el cual el cliente solicitó alguna modificación.
 */
public class ProductoAjustado implements Producto
{
    private ProductoMenu productoBase;

    /** Ingredientes que el usuario quiere agregar  */
    private ArrayList<Ingrediente> agregados;

    /** Ingredientes que el usuario quiere eliminar */
    private ArrayList<Ingrediente> eliminados;

    /**
     * Construye un nuevo producto ajustado a partir del producto base y sin modificaciones
     * @param productoBase El producto base que se va a ajustar
     */
    public ProductoAjustado(ProductoMenu productoBase)
    {
        this.productoBase = productoBase;
        this.agregados = new ArrayList<>();
        this.eliminados = new ArrayList<>();
    }

    @Override
    public String getNombre()
    {
        return productoBase.getNombre();
    }

    /**
     * Precio = precio del producto base + suma de costos de ingredientes agregados.
     * (Eliminar ingredientes NO cambia el precio).
     */
    @Override
    public int getPrecio()
    {
        int total = productoBase.getPrecio();
        for (Ingrediente ing : agregados) {
            total += ing.getCostoAdicional();
        }
        return total;
    }

    /** Registra un ingrediente agregado por el cliente. */
    public void agregarIngrediente(Ingrediente ingrediente)
    {
        agregados.add(ingrediente);
    }

    /** Registra un ingrediente eliminado por el cliente. */
    public void eliminarIngrediente(Ingrediente ingrediente)
    {
        eliminados.add(ingrediente);
    }

    /**
     * Genera el texto que debe aparecer en la factura:
     * - Factura del producto base
     * - Ingredientes agregados con su costo
     * - Ingredientes eliminados
     * - Total del producto ajustado
     */
    @Override
    public String generarTextoFactura()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(productoBase.generarTextoFactura());

        // 2) agregados
        for (Ingrediente ing : agregados) {
            sb.append("    +").append(ing.getNombre()).append("\n");
            sb.append("                ").append(ing.getCostoAdicional()).append("\n");
        }

        // 3) eliminados
        for (Ingrediente ing : eliminados) {
            sb.append("    -").append(ing.getNombre()).append("\n");
        }

        // 4) total del ajustado
        sb.append("            ").append(getPrecio()).append("\n");

        return sb.toString();
    }
}
