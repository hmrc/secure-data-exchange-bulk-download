import sbt.{ForkOptions, TestDefinition}
import sbt.Tests.{Group, SubProcess}

object TestPhases {
  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map {
      test => Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name))))
    }
}
