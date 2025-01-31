package org.codehaus.mojo.versions.ordering;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.mojo.versions.api.Segment;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class VersionComparatorsTest
{
    private final String[] versionDataset = {
        "1",
        "1.0",
        "1.0.0",
        "1.0.0-1",
        "1.0.0-sp1",
        "foobar",
        "1-alpha-1",
    };

    @Test
    public void testMavenVersionComparator() throws InvalidSegmentException
    {
        assertVersions( new MavenVersionComparator() );
    }

    @Test
    public void testMercuryVersionComparator() throws InvalidSegmentException
    {
        assertVersions( new MercuryVersionComparator() );
    }

    @Test
    public void testNumericVersionComparator() throws InvalidSegmentException
    {
        assertVersions( new NumericVersionComparator() );
    }

    public void assertVersions( VersionComparator instance ) throws InvalidSegmentException
    {
        for ( String s : versionDataset )
        {
            assertLater( s, instance );
            assertLater( s + "-SNAPSHOT", instance );
        }
    }

    public void assertLater( String version, VersionComparator instance ) throws InvalidSegmentException
    {
        ArtifactVersion v1 = new DefaultArtifactVersion( version );
        int count = instance.getSegmentCount( v1 );
        for ( int i = 0; i < count; i++ )
        {
            ArtifactVersion v2 = instance.incrementSegment( v1, Segment.of( i ) );
            assertTrue( v1 + " < " + v2, instance.compare( v1, v2 ) < 0 );
        }
    }
}
