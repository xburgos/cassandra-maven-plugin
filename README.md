# Required Configuration
              
              <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>cassandra-maven-plugin</artifactId>
                <version>2.0.0-1-klappo</version>
                <configuration>
                    <cqlVersion>3.0</cqlVersion>
                    <keyspace>userservice</keyspace>
                    <addTestClasspath>true</addTestClasspath>
                    <startNativeTransport>true</startNativeTransport>
                    <stopPort>8070</stopPort>
                    <stopKey>stop-cassandra</stopKey>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>start</goal>
                            <goal>stop</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
