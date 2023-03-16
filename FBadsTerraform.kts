provider "aws" {
  region = "us-east-1"
}

provider "facebook" {
  access_token = var.fb_access_token
}

resource "aws_instance" "db" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  key_name      = "my-key-pair"
  subnet_id     = "subnet-0123456789abcdef0"
  vpc_security_group_ids = [
    "sg-0123456789abcdef0",
    "sg-0123456789abcdef1",
  ]

  tags = {
    Name = "example-db-instance"
  }
}

resource "aws_db_instance" "example" {
  allocated_storage    = 20
  engine               = "mysql"
  engine_version       = "5.7"
  instance_class       = "db.t2.micro"
  name                 = "example-db"
  username             = "admin"
  password             = "password123"
  parameter_group_name = "default.mysql5.7"
  skip_final_snapshot  = true

  tags = {
    Name = "example-db"
  }
}

resource "aws_security_group" "allow_mysql_access" {
  name_prefix = "allow_mysql_access"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "facebook_business_ad_account" "example" {
  account_id = var.fb_account_id
}

resource "facebook_business_ad_campaign" "example" {
  account_id = facebook_business_ad_account.example.id
  name       = "Example Campaign"
}

resource "facebook_business_ad" "example" {
  account_id  = facebook_business_ad_account.example.id
  campaign_id = facebook_business_ad_campaign.example.id
  name        = "Example Ad"
  adset_spec {
    targeting {
      age_min = 18
      age_max = 30
    }
    daily_budget = 5000
  }
}

resource "null_resource" "run_ads_api" {
  provisioner "local-exec" {
    command = "curl -G 'https://graph.facebook.com/v12.0/${facebook_business_ad.example.id}/insights' --data-urlencode 'fields=impressions,clicks,spend' --data-urlencode 'access_token=${var.fb_access_token}' | mysql -h ${aws_db_instance.example.address} -P 3306 -u ${aws_db_instance.example.username} -p${aws_db_instance.example.password} example-db"
  }
}
