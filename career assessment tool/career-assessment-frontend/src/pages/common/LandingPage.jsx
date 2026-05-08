import { Link } from 'react-router-dom';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import Button from '../../components/common/Button';
import {
  GraduationCap,
  Brain,
  Wrench,
  Star,
  ArrowRight,
  CheckCircle,
  Users,
  BarChart2,
} from 'lucide-react';

const features = [
  {
    icon: Brain,
    title: 'Personality Assessment',
    description: 'Discover your unique personality traits using proven psychological frameworks like MBTI.',
    color: 'bg-purple-50 text-purple-600',
  },
  {
    icon: Wrench,
    title: 'Skills Evaluation',
    description: 'Identify your core technical and soft skills through comprehensive competency tests.',
    color: 'bg-blue-50 text-blue-600',
  },
  {
    icon: Star,
    title: 'Interest Mapping',
    description: 'Explore your professional interests with Holland Code (RIASEC) methodology.',
    color: 'bg-yellow-50 text-yellow-600',
  },
  {
    icon: BarChart2,
    title: 'Career Recommendations',
    description: 'Receive AI-powered career path recommendations tailored to your unique profile.',
    color: 'bg-green-50 text-green-600',
  },
];

const steps = [
  { step: '01', title: 'Create Account', desc: 'Register as a student in minutes.' },
  { step: '02', title: 'Take Assessments', desc: 'Complete personality, skills & interest tests.' },
  { step: '03', title: 'Get Results', desc: 'View detailed analysis of your profile.' },
  { step: '04', title: 'Explore Careers', desc: 'Discover career paths matched to you.' },
];

const stats = [
  { value: '10,000+', label: 'Students Assessed' },
  { value: '200+', label: 'Career Paths' },
  { value: '95%', label: 'Satisfaction Rate' },
  { value: '50+', label: 'Partner Institutions' },
];

const LandingPage = () => (
  <div className="min-h-screen flex flex-col">
    <Navbar />

    {/* Hero */}
    <section className="bg-gradient-to-br from-indigo-600 via-indigo-700 to-purple-700 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 lg:py-32 text-center">
        <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full text-sm font-medium mb-8 border border-white/20">
          <GraduationCap size={16} />
          <span>Career Assessment & Recommendation Platform</span>
        </div>
        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold leading-tight mb-6">
          Discover Your <span className="text-yellow-300">Ideal Career</span>
          <br />Path Today
        </h1>
        <p className="text-xl text-indigo-100 max-w-2xl mx-auto mb-10 leading-relaxed">
          Empowering students with data-driven career guidance through intelligent personality, skills, and interest assessments.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to="/register">
            <Button size="xl" className="bg-white text-indigo-700 hover:bg-indigo-50 font-bold shadow-lg">
              Get Started Free <ArrowRight size={20} />
            </Button>
          </Link>
          <Link to="/login">
            <Button size="xl" variant="outline" className="border-white text-white hover:bg-white/10">
              Sign In
            </Button>
          </Link>
        </div>
      </div>
    </section>

    {/* Stats */}
    <section className="bg-white border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-8 text-center">
          {stats.map((s) => (
            <div key={s.label}>
              <p className="text-4xl font-extrabold text-indigo-600">{s.value}</p>
              <p className="text-gray-500 mt-1 text-sm">{s.label}</p>
            </div>
          ))}
        </div>
      </div>
    </section>

    {/* Features */}
    <section className="bg-gray-50 py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-14">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900">Everything You Need</h2>
          <p className="text-gray-500 mt-3 text-lg">Comprehensive tools to guide your career journey</p>
        </div>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((f) => (
            <div key={f.title} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${f.color}`}>
                <f.icon size={24} />
              </div>
              <h3 className="font-semibold text-gray-900 mb-2">{f.title}</h3>
              <p className="text-gray-500 text-sm leading-relaxed">{f.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>

    {/* How it works */}
    <section className="bg-white py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-14">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900">How It Works</h2>
          <p className="text-gray-500 mt-3 text-lg">Four simple steps to your career clarity</p>
        </div>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {steps.map((s) => (
            <div key={s.step} className="text-center">
              <div className="w-16 h-16 bg-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-4 text-white font-bold text-xl shadow-lg">
                {s.step}
              </div>
              <h3 className="font-semibold text-gray-900 text-lg mb-2">{s.title}</h3>
              <p className="text-gray-500 text-sm">{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>

    {/* Benefits */}
    <section className="bg-indigo-600 py-20 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="lg:flex items-center gap-16">
          <div className="flex-1 mb-10 lg:mb-0">
            <h2 className="text-3xl sm:text-4xl font-bold mb-6">Why Choose CareerPath?</h2>
            <ul className="space-y-4">
              {[
                'Science-backed personality and skills assessment',
                'Personalized career recommendations',
                'Comprehensive career path database',
                'Progress tracking and detailed reports',
                'Mobile-friendly interface',
                'Free for all registered students',
              ].map((item) => (
                <li key={item} className="flex items-center gap-3 text-indigo-100">
                  <CheckCircle size={20} className="text-yellow-300 shrink-0" />
                  {item}
                </li>
              ))}
            </ul>
          </div>
          <div className="flex-1 text-center">
            <div className="bg-white/10 backdrop-blur-sm rounded-3xl p-10 border border-white/20">
              <Users size={64} className="mx-auto mb-6 text-yellow-300" />
              <h3 className="text-2xl font-bold mb-2">Join 10,000+ Students</h3>
              <p className="text-indigo-200 mb-8">Take the first step towards a fulfilling career today.</p>
              <Link to="/register">
                <Button size="lg" className="bg-yellow-400 text-gray-900 hover:bg-yellow-300 font-bold">
                  Start Your Assessment <ArrowRight size={18} />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>

    <Footer />
  </div>
);

export default LandingPage;
