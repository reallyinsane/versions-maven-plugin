package org.codehaus.mojo.versions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class SetMojoTest extends AbstractMojoTestCase
{
    @Rule
    public MojoRule mojoRule = new MojoRule( this );

    @Test
    public void testGetIncrementedVersion() throws MojoExecutionException
    {
        new SetMojo( null, null, null, null, null, null )
        {
            {
                assertThat( getIncrementedVersion( "1.0.0", null ), is( "1.0.1-SNAPSHOT" ) );
                assertThat( getIncrementedVersion( "1.0.0-SNAPSHOT", null ), is( "1.0.1-SNAPSHOT" ) );
                assertThat( getIncrementedVersion( "1.0.0-SNAPSHOT", 1 ), is( "2.0.0-SNAPSHOT" ) );
                assertThat( getIncrementedVersion( "1.0.0-SNAPSHOT", 2 ), is( "1.1.0-SNAPSHOT" ) );
                assertThat( getIncrementedVersion( "1.0.0-SNAPSHOT", 3 ), is( "1.0.1-SNAPSHOT" ) );
            }
        };
    }

    @Test
    public void testNextSnapshotIndexLowerBound()
    {
        new SetMojo( null, null, null, null, null, null )
        {
            {
                try
                {
                    getIncrementedVersion( "1.0.0", 0 );
                    fail();
                }
                catch ( MojoExecutionException e )
                {
                    assertThat( e.getMessage(),
                            containsString( "nextSnapshotIndexToIncrement cannot be less than 1" ) );
                }
            }
        };
    }

    @Test
    public void testNextSnapshotIndexUpperBound()
    {
        new SetMojo( null, null, null, null, null, null )
        {
            {
                try
                {
                    getIncrementedVersion( "1.0.0", 4 );
                    fail();
                }
                catch ( MojoExecutionException e )
                {
                    assertThat( e.getMessage(), containsString(
                            "nextSnapshotIndexToIncrement cannot be greater than the last version index" ) );
                }
            }
        };
    }

    @Test
    public void testNextSnapshotIndexWithoutNextSnapshot() throws MojoFailureException
    {
        try
        {
            new SetMojo( null, null, null, null, null, null )
            {
                {
                    project = new MavenProject();
                    project.setParent( new MavenProject() );
                    project.setOriginalModel( new Model() );
                    project.getOriginalModel().setVersion( "1.2.3-SNAPSHOT" );

                    nextSnapshotIndexToIncrement = 4;
                }
            }.execute();
        }
        catch ( MojoExecutionException e )
        {
            assertThat( e.getMessage(),
                    containsString( "nextSnapshotIndexToIncrement is not valid when nextSnapshot is false" ) );
        }
    }

    @Test
    public void testVersionlessDependency() throws Exception
    {
        SetMojo myMojo = (SetMojo) mojoRule.lookupConfiguredMojo(
                new File( "target/test-classes/org/codehaus/mojo/set/versionless-01" ), "set" );
        myMojo.execute();
    }

    @Test
    public void testRemoveSnapshotIdempotency()
            throws Exception
    {
        Path pomDir = Files.createTempDirectory( "set-" );
        try
        {
            Files.copy( Paths.get( "src/test/resources/org/codehaus/mojo/set/remove-snapshot/pom.xml" ),
                    Paths.get( pomDir.toString(),  "pom.xml" ), REPLACE_EXISTING );

            SetMojo firstRun = (SetMojo) mojoRule.lookupConfiguredMojo( pomDir.toFile(), "set" );
            firstRun.execute();
            assertThat( String.join( "", Files.readAllLines( Paths.get( pomDir.toString(), "pom.xml" ) ) ),
                    containsString( "<version>1.0</version>" ) );

            // no exception should be thrown, the file should stay with version "1.0"
            SetMojo secondRun = (SetMojo) mojoRule.lookupConfiguredMojo( pomDir.toFile(), "set" );
            MavenExecutionRequest request =
                    (MavenExecutionRequest) getVariableValueFromObject( secondRun.settings, "request" );
            setVariableValueToObject( request, "interactiveMode", false );
            secondRun.execute();
            assertThat( String.join( "", Files.readAllLines( Paths.get( pomDir.toString(), "pom.xml" ) ) ),
                    containsString( "<version>1.0</version>" ) );
        }
        finally
        {
            if ( pomDir != null && pomDir.toFile().exists() )
            {
                Arrays.stream( Objects.requireNonNull( pomDir.toFile().listFiles() ) ).forEach( File::delete );
                pomDir.toFile().delete();
            }
        }
    }
}
