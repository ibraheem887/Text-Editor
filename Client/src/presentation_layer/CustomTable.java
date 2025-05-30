package presentation_layer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CustomTable extends JTable {
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);

       
        if (row % 2 == 0) {
            comp.setBackground(new Color(240, 248, 255)); 
        } else {
            comp.setBackground(new Color(255, 255, 255)); 
        }

        
        String cellValue = getValueAt(row, column).toString();
        boolean isArabic = cellValue.codePoints().anyMatch(
            c -> (c >= 0x0600 && c <= 0x06FF) || (c >= 0x0750 && c <= 0x077F) || (c >= 0x08A0 && c <= 0x08FF)
        );

        if (isArabic) {
           
            comp.setFont(new Font("Arial", Font.PLAIN, 18)); 
            if (column == 0) {
                ((JLabel) comp).setHorizontalAlignment(SwingConstants.RIGHT); 
            }
            comp.setForeground(new Color(139, 0, 139));
        } else {
            
            comp.setFont(new Font("Arial", Font.PLAIN, 14)); 
            if (column == 0) {
                comp.setForeground(new Color(0, 51, 102)); 
            } else {
                comp.setForeground(new Color(34, 139, 34)); 
            }
        }

        return comp;
    }
}
