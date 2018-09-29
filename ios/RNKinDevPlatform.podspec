Pod::Spec.new do |s|
  s.name         = "RNKinDevPlatform"
  s.version      = "1.0.0"
  s.summary      = "RNKinDevPlatform"
  s.description  = <<-DESC
                  RNKinDevPlatform
                   DESC
  s.homepage         = 'https://kinecosystem.org'
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "8.1"
  s.source       = { :git => "https://github.com/author/RNKinDevPlatform.git", :tag => "master" }
  s.source_files  = "*.{h,m,swift}"
  s.requires_arc = true


  s.swift_version = '4.1'
  s.dependency "React"
  s.dependency "KinDevPlatform", "0.8.2"

end
