package mymojo

/**
 * This is a mojo.
 *
 * @goal mymojo
 */
class MyMojo4
    extends MojoSupport
{
    /**
     * @parameter expression="${mymojo.flag}" default-value="false"
     */
    boolean flag

    boolean anotherFlag

    /**
     * foo
     */
    def foo

    def foo() {
        def someLocal
    }
}