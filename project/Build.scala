import sbt._
import Project._
import Keys._
import Defaults._

object Build extends Build {

  lazy val buildSettings = Seq(
    organization       := "info.folone",
    version            := "0.11-SNAPSHOT",

    scalaVersion       := "2.10.2",
    crossScalaVersions := Seq("2.9.3", "2.10.2"),

    scalacOptions      := Seq(
      "-encoding", "UTF-8",
      "-deprecation",
      "-unchecked",
      "-explaintypes"
    ),

    parallelExecution in Compile := true
  )

  lazy val repoSettings = Seq(
    resolvers ++= Seq(
      "releases"  at "https://oss.sonatype.org/content/repositories/releases",
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots")
  )

  lazy val publishSetting = publishTo <<= (version).apply{
    v ⇒
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  lazy val credentialsSetting = credentials += {
    Seq("build.publish.user", "build.publish.password").map(k ⇒ Option(System.getProperty(k))) match {
      case Seq(Some(user), Some(pass)) ⇒
        Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
      case _                           ⇒
        Credentials(Path.userHome / ".ivy2" / ".credentials")
    }
  }

  lazy val standardSettings = super.settings     ++
    Defaults.defaultSettings                     ++
    buildSettings                                ++
    sbtrelease.ReleasePlugin.releaseSettings     ++
    org.scalastyle.sbt.ScalastylePlugin.Settings ++
    repoSettings                                 ++
    Seq(
      name := "poi-scala",
      resolvers += Resolver.sonatypeRepo("releases"),
      libraryDependencies <++= (scalaVersion) { sv ⇒
        Seq(
          "org.apache.poi" %  "poi"                       % "3.9",
          "org.apache.poi" %  "poi-ooxml"                 % "3.9",
          "org.scalaz"     %% "scalaz-core"               % "7.1.0-M2",
          "org.scalaz"     %% "scalaz-effect"             % "7.1.0-M2",
          "org.specs2"     %% "specs2"                    % Dependencies.specs2(sv) % "test",
          "org.scalacheck" %% "scalacheck"                % "1.10.1"                % "test",
          "org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.0-M2"              % "test"
        )
      },
      credentialsSetting,
      publishSetting,
         pomExtra := (
           <url>https://github.com/folone/poi.scala</url>
           <licenses>
             <license>
               <name>Apache License</name>
               <url>http://opensource.org/licenses/Apache-2.0</url>
               <distribution>repo</distribution>
             </license>
           </licenses>
           <scm>
             <url>git@github.com:folone/poi.scala.git</url>
             <connection>scm:git:git@github.com:folone/poi.scala.git</connection>
           </scm>
           <developers>
           {
             Seq(
               ("folone",       "George Leontiev"),
               ("fedgehog",     "Maxim Fedorov"),
               ("Michael Rans", "Michael Rans"),
               ("daneko",       "Kouichi Akatsuka")
             ).map { case (id, name) ⇒
               <developer>
                 <id>{id}</id>
                 <name>{name}</name>
                 <url>http://github.com/{id}</url>
               </developer>
             }
           }
           </developers>
         )
    )

  override lazy val settings = super.settings ++ repoSettings  ++ Seq(
    shellPrompt := { s ⇒ Project.extract(s).currentProject.id + " > " }
  )

  lazy val excludedTestNames = SettingKey[Seq[String]]("excluded-tests", "temporary excluded tests")

  lazy val poi = Project(
    id = "poi",
    base = file("."),
    settings = standardSettings
  )

  object Dependencies {
    def specs2(scalaVersion: String) =
      if (scalaVersion startsWith "2.9")
        "1.12.4.1"
      else
        "2.2-scalaz-7.1.0-M2-SNAPSHOT"
  }
}
