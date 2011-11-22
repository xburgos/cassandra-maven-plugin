package org.apache.maven.diagrams.connectors.classes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prefuse.data.Graph;
import prefuse.data.Node;



public class PackageUtilsOldTest {
    
    public static Graph createGraph(InputStream is, URL classpath[]) throws IOException
    {
        List<String> res=PackageUtilsOld.getClasseNamesInPackage(is, "");
        
        Set<String> nodes=new HashSet<String>();
        nodes.add("-");
        
        Set<Para> transitions=new HashSet<Para>();
        System.out.println("Found "+res.size()+" classes");
    
        URLClassLoader jarLoader = new URLClassLoader(classpath);
             
        for(int i=0; i<res.size(); i++)
        {
            String className=((String)res.get(i)).substring(0, ((String)res.get(i)).length()-6);
            try{
                Class<?> c=jarLoader.loadClass(className);
                nodes.add(c.toString()  );
                Type d=c;
                while ((Class.class.isInstance(d)&&(((Class<?>)d).getSuperclass()!=null)))
                {
                    String s=((Class<?>)d).getGenericSuperclass().toString();
                    
                    nodes.add(s);
                    transitions.add(new Para(s,d.toString()));
                    d=((Class<?>)d).getGenericSuperclass();
                }
                transitions.add(new Para("-",d.toString()));
            }catch (ClassNotFoundException e) {
                System.err.println(i+" ClassNotFound:: "+className);
            }catch (NoClassDefFoundError e) {
                System.err.println(i+" NoClassDefFoundError: "+className);
            }catch (VerifyError e){
                System.err.println(i+" VerifyError: "+className);
            }catch (Throwable e) {
                System.err.println(i+" "+e.getMessage());
            }
        }
        

        Graph g=new Graph(true);
        g.addColumn("name", String.class);
        g.addColumn("gender", String.class);
        HashMap<String,Node> mapka=new HashMap<String, Node>();
        
        Node n=g.addNode();
        n.setString( "name", "-" );
        n.setString( "gender", "-" );
        mapka.put( "-", n);
        
        for(String s:nodes)
        {
            if (!s.equals( "-" ))
            {
                Node t=g.addNode();
                t.setString("name", s);
                t.setString("gender", s.contains("class")?"M":"F");
                mapka.put(s,t);
                System.out.println("Name: "+s);
            }
        }
        
        for(Para p:transitions)
        {
            Node n1=(Node)mapka.get(p.from);
            Node n2=(Node)mapka.get(p.to);
            if ((n1!=null)&&(n2!=null))
                g.addEdge(n1,n2);
            
            System.out.println("Connecting: "+p.from+" "+p.to);
        }
        
        System.err.println("Nodes: "+nodes.size());
        System.err.println("Edges: "+transitions.size());
        
        return g;
    }



}
