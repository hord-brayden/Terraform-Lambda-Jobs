# Terraform-Lambda-Jobs

This is a repo consisting of various functions setup through terraform into an AWS acct, and via that hcl job, it will also provision lambda resources and setup a cron scheduler and daemon to run the lambda job. The goal of this project is to continuously increment the viability of serverless async jobs to the point where a user can upload a terra file and set everything up from one point.

Issues:
One of the biggest issues currently is provisioning keys and global variables through a terraform job so that it's not stored in the lambda job and potentially vulnerable. There are a few ways to solve this issue, but, I'd like to keep everything in 1 job to keep marching towards the end goal of having this be a 1-click solution.
