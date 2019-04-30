# cloud-governance
Governance in the Cloud

## Configure Stack Sets
You need to set up the environment by creating the administrator role that will assume the execution role in the destination account. So in order for this to work, you need to have the `Administrator` and `Executor` role.

1. Create the `Administrator` role by deploying the [AWSCloudFormationStackSetAdministrationRole.yml](AWSCloudFormationStackSetAdministrationRole.yml) template
2. Take note of the account number that will act as the administrator, you'll use it for the `Executor` role
3. Create the `Executor` role by deploying the [AWSCloudFormationStackSetExecutionRole.yml](AWSCloudFormationStackSetExecutionRole.yml). In the parameter section, enter the account number of the administartor account.

## Create the Jenkins Deployment Role

1. Create a CloudFormation stackset using the [AWSJenkinsAdministrationRole.yml](AWSJenkinsAdministrationRole.yml) template, you'll need to deploy it to at least one account for this to work. The template requires an account number that the role will "trus" when assuming the role to create any AWS resource in Jenkins.
2. If you want to deploy the same template to any other account, enter to the stack and click on "Manage StackSet" and enter the AWS account destination number and the region.

If you want to perform a change in the template role, you can do the change in the template and update the stack set. Then, apply the updates to the AWS accounts.

## Pre-Commit Hook
Before you can set up the pre-commit hook, you need to make sure that you have Python and pip installed. Then, you need to install the `pre-commit` framework with the following command `sudo pip install pre-commit`.

Create the `.pre-commit-config.yaml` in the root of the repository with the following content:

```
# .pre-commit-config.yaml
repos:
-   repo: https://github.com/awslabs/cfn-python-lint
    rev: v0.15.0 # The version of cfn-lint to use
    hooks:
    -   id: cfn-python-lint
    files: src/.*\.(json|yml|yaml)$
```

Finally, install the hook with the following command `pre-commit install`.