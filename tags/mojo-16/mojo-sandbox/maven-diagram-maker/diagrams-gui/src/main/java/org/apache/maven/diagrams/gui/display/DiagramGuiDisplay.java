package org.apache.maven.diagrams.gui.display;

public class DiagramGuiDisplay extends prefuse.Display
{
//        
////        private Vizster vizster;
////        private AffineTransform id = new AffineTransform();
////        //private Legend legend = null;
//        
//        public DiagramGuiDisplay(Vizster vizster) {
//            super(vizster.getItemRegistry());
//   //         this.vizster = vizster;
//        } //
//        
//        public void prePaint(Graphics2D g) {
//            Object o = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
//            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//            AffineTransform at = g.getTransform();
//            g.setTransform(id);
//            
//            Color c = ColorLib.getColor(200,200,200,255);
//            Font f = FontLib.getFont("SansSerif",Font.BOLD|Font.ITALIC,48);
//            FontMetrics fm = g.getFontMetrics(f);
//            String s = "vizster";
//            int x = 8, y = fm.getAscent();
//            g.setColor(c);
//            g.setFont(f);
//            g.drawString(s,x,y);
//            if ( vizster.isXRayMode() ) {
//                f = FontLib.getFont("SansSerif",Font.BOLD|Font.ITALIC,18);
//                fm = g.getFontMetrics(f);
//                s = "x-ray";
//                g.setFont(f);
//                g.drawString(s,140,fm.getAscent()-2);
//            }
//            
//            if ( legend != null ) {
//                x = this.getWidth()-legend.getWidth();
//                legend.paint(g,this,x,0);
//            }
//            
//            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, o);
//            g.setTransform(at);
//        } //
//        
//        public void setLegend(Legend l) {
//     //      legend = l;
//        } //
//        
//    } // end of class VizsterDisplay

}
